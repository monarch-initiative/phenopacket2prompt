package org.monarchinitiative.phenopacket2prompt.output.impl.czech;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * This class produces the very first sentence of the prompt, e.g.,
 * <pre>
 * "The proband was a 22-year-old woman."
 * </pre>
 * <p>
 * There are several possible cases to take into account, depending on whether we have the
 * the age at last presentation and whether the sex is specified.
 * The proband was a female.
 * The proband was 33-years old.
 * The proband was a 22-year-old man.
 * The proband was a male fetus.
 * If neither age nor sex is available, we write
 * "The proband was an individual."
 * </p>
 * <p>
 * The entry point to the generation of this sentence is the function {@code #getIndividualDescription}.
 * </p>
 */
public class PpktIndividualCzech implements PPKtIndividualInfoGenerator {

    private final BuildingBlockGenerator buildBlocks;

    public PpktIndividualCzech() {
        buildBlocks = new CzechBuildingBlocks();
    }

    /**
     * We begin our description with a sentence
     *
     * @param individual the individual for whom we are creating a diagnostic prompt
     * @return complete prompt for an LLM
     */
    public String getIndividualDescription(PpktIndividual individual) {
        if (individual.annotationCount() == 0) {
            throw new PhenolRuntimeException("No HPO annotations");
        }

        Optional<PhenopacketAge> lastExamOpt = individual.getAgeAtLastExamination();
        Optional<PhenopacketAge> onsetOpt = individual.getAgeAtOnset();
        PhenopacketSex psex = individual.getSex();
        String individualDescription;
        String onsetDescription;
        if (lastExamOpt.isPresent()) {
            var lastExamAge =  lastExamOpt.get();
            if (lastExamAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
                Iso8601Age isoAge = (Iso8601Age) lastExamAge;
                individualDescription =  iso8601individualDescription(psex, isoAge);
            } else if (lastExamAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
                HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) lastExamAge;
                individualDescription = hpoOnsetIndividualDescription(psex, hpoOnsetTermAge);
            } else {
                // should never happen
                throw new PhenolRuntimeException("Did not recognize last exam age type " + lastExamAge.ageType());
            }
        }  else {
            individualDescription =  switch (psex) {
                case FEMALE -> buildBlocks.probandWasAFemale();
                case MALE -> buildBlocks.probandWasAMale();
                default -> buildBlocks.probandWasAnIndividual();
            };
        }
        if (onsetOpt.isPresent()) {
            var onsetAge = onsetOpt.get();
            if (onsetAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
                Iso8601Age isoAge = (Iso8601Age) onsetAge;
                onsetDescription =  iso8601onsetDescription(isoAge, psex);
            } else if (onsetAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
                HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) onsetAge;
                onsetDescription = hpoOnsetDescription(hpoOnsetTermAge, psex);
            } else {
                // should never happen
                throw new PhenolRuntimeException("Did not recognize last exam age type " + onsetAge.ageType());
            }
        } else {
            onsetDescription = "Nástup onemocnění nebyl specifikován";
        }
        return String.format("%s. %s.", individualDescription, onsetDescription);

    }



    private String iso8601onsetDescription(Iso8601Age isoAge, PhenopacketSex psex) {
        String proband = switch (psex) {
            case FEMALE -> Nouns.PROBAND.genitiv(Genus.SHE);
            // Male or generic masculinity
            case MALE, OTHER, UNKNOWN -> Nouns.PROBAND.genitiv(Genus.HE);
        };
        return String.format("První projevy onemocnění se u %s objevily ve věku %s",
                proband,
                buildBlocks.yearsMonthsDaysOld(isoAge.getYears(), isoAge.getMonths(), isoAge.getDays()));
    }

    private String hpoOnsetDescription(HpoOnsetAge hpoOnsetTermAge, PhenopacketSex psex) {
        String proband = switch (psex) {
            case FEMALE -> Nouns.PROBAND.genitiv(Genus.SHE);
            // Male or generic masculinity
            case MALE, OTHER, UNKNOWN -> Nouns.PROBAND.genitiv(Genus.HE);
        };
        return String.format("První projevy onemocnění se u %s objevily %s",
            proband,
            nameOfLifeStage(hpoOnsetTermAge));
    }

    private String nameOfLifeStage(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return "v prenatálním období";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return "v perinatálním období";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "v kojeneckém věku";
        } else if (hpoOnsetTermAge.isChild()) {
            return "v detství";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "v období dospívání";
        } else if (hpoOnsetTermAge.isNeonate()) {
            return "v novorozeneckém období";
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return "v mladé dospělosti";
        }  else if (hpoOnsetTermAge.isAdult()) {
            return "v dospělosti";
        } else {
            throw new PhenolRuntimeException("Could not identify life stage name for HpoOnsetAge " + hpoOnsetTermAge.toString());
        }
    }


    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> Nouns.FEMALE.nominativ(Genus.SHE);
            case MALE -> Nouns.MALE.nominativ(Genus.HE);
            default -> Nouns.INDIVIDUAL.nominativ(Genus.HE);
        };
    }


    /**
     * Create a phrase such as "at the age of 7 years, 4 months, and 2 days"
     * Leave out the months and days if they are zero.
     *
     * @param isoAge
     * @return
     */
    private String iso8601AtAgeOf(Iso8601Age isoAge) {
        List<String> components = new ArrayList<>();

        if (isoAge.getYears() > 0) {
            components.add(buildBlocks.years(isoAge.getYears()));
        }
        if (isoAge.getMonths() > 0) {
            components.add(buildBlocks.months(isoAge.getMonths()));
        }
        if (isoAge.getDays() > 0) {
            components.add(buildBlocks.months(isoAge.getDays()));
        }
        if (components.isEmpty()) {
            return buildBlocks.asNewborn();
        } else if (components.size() == 1) {
            return buildBlocks.atTheAgeOf() + " " + components.getFirst();
        } else if (components.size() == 2) {
            return buildBlocks.atTheAgeOf() + " " + components.get(0) + " a " + components.get(1);
        } else {
            return buildBlocks.atTheAgeOf() + " " + components.get(0) + ", " + components.get(1) +
                    " a " + components.get(2);
        }
    }



    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        // if older
        if (y > 17) {
            String probandWasA = switch (psex) {
                case FEMALE -> Nouns.PROBAND.nominativ(Genus.SHE) + " byla";
                case MALE -> Nouns.PROBAND.nominativ(Genus.HE) + " byl"; // muz/chlapec/
                case OTHER, UNKNOWN -> Nouns.PROBAND.nominativ(Genus.HE) + " byla"; // ... osoba
            };
            probandWasA = capitalizeFirstCharacter(probandWasA);
            String nYearOld = switch (psex) {
                case FEMALE, OTHER, UNKNOWN -> "%s letá".formatted(y);
                case MALE -> "%s letý".formatted(y);
            };
            String individual = switch (psex) {
                case FEMALE -> buildBlocks.woman();
                case MALE -> buildBlocks.man();
                default -> buildBlocks.individual();
            };

            return String.format("%s %s %s", probandWasA, nYearOld, individual);
        } else if (y > 9) {
            String probandWasA = switch (psex) {
                case FEMALE -> Nouns.PROBAND.nominativ(Genus.SHE) + " byla"; // divka
                case MALE -> Nouns.PROBAND.nominativ(Genus.HE) + " byl"; // chlapec/
                case OTHER, UNKNOWN -> Nouns.PROBAND.nominativ(Genus.HE) + " bylo"; // ... dítě
            };
            probandWasA = capitalizeFirstCharacter(probandWasA);
            String nYearOld = switch (psex) {
                case FEMALE -> "%s letá".formatted(y);
                case MALE -> "%s letý".formatted(y);
                case OTHER, UNKNOWN -> "%s leté".formatted(y);
            };
            String individual = switch (psex) {
                case FEMALE -> buildBlocks.adolescentGirl();
                case MALE -> buildBlocks.adolescentBoy();
                default -> buildBlocks.adolescentChild();
            };
            return String.format("%s %s %s", probandWasA, nYearOld, individual);
        } else if (y > 0) {
            String probandWasA = switch (psex) {
                case FEMALE -> Nouns.PROBAND.nominativ(Genus.SHE) + " byla"; // divka
                case MALE -> Nouns.PROBAND.nominativ(Genus.HE) + " byl"; // chlapec/
                case OTHER, UNKNOWN -> Nouns.PROBAND.nominativ(Genus.HE) + " bylo"; // ... dítě
            };
            probandWasA = capitalizeFirstCharacter(probandWasA);
            String xYearsYMonthsAndZDaysOld = buildBlocks.yearsMonthsDaysOld(y, m, d);
            String individual = switch (psex) {
                case FEMALE -> buildBlocks.girl();
                case MALE -> buildBlocks.boy();
                default -> buildBlocks.child();
            };
            return String.format("%s %s %s", probandWasA, individual, xYearsYMonthsAndZDaysOld);
        } else if (m > 0 || d > 0) {
            String probandWasA = Nouns.PROBAND.nominativ(Genus.HE) + " byl"; // kojenec
            probandWasA = capitalizeFirstCharacter(probandWasA);
            String ofMaleFemaleUnspecifiedSex = switch (psex) {
                case FEMALE -> Adjectives.FEMALE.genitivNeutrum();
                case MALE -> Adjectives.MALE.genitivNeutrum();
                case OTHER, UNKNOWN -> Adjectives.UNSPECIFIED.genitivNeutrum();
            };
            String inAgeOfYMonthsAndZDays = buildBlocks.monthDayOld(m, d);

            return String.format("%s kojenec %s pohlaví %s", probandWasA, ofMaleFemaleUnspecifiedSex, inAgeOfYMonthsAndZDays);
        } else {
            String probandWasA = Nouns.PROBAND.nominativ(Genus.HE) + " byl"; // novorozenec
            probandWasA = capitalizeFirstCharacter(probandWasA);
            String ofMaleFemaleUnspecifiedSex = switch (psex) {
                case FEMALE -> Adjectives.FEMALE.genitivNeutrum();
                case MALE -> Adjectives.MALE.genitivNeutrum();
                case OTHER, UNKNOWN -> Adjectives.UNSPECIFIED.genitivNeutrum();
            };
            String inAgeOfYMonthsAndZDays = buildBlocks.monthDayOld(m, d);
            return String.format("%s novorozenec %s pohlaví %s", probandWasA, ofMaleFemaleUnspecifiedSex, inAgeOfYMonthsAndZDays);
        }
    }

    private static String capitalizeFirstCharacter(String val) {
        return val.substring(0, 1).toUpperCase() + val.substring(1);
    }


    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.femaleFetus());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.maleFetus());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.fetus());
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.newbornGirl());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.newbornBoy());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.newborn());
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.femaleInfant());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.maleInfant());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.infant());
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.girl());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.boy());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.child());
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.adolescentGirl());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.adolescentBoy());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.adolescentChild());
            };
        } else if (hpoOnsetTermAge.isAdult()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.woman());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.male());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.adult());
            };
        } else {
            throw new PhenolRuntimeException("Could not find HPO onset type " + hpoOnsetTermAge.toString());
        }
    }


    @Override
    public String atAgeForVignette(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "Ve věku " + buildBlocks.fromIso((Iso8601Age)ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            if (ppktAge.isEmbryo()) {
                return buildBlocks.duringEmbryonic();
            } else if (ppktAge.isFetus()) {
                return buildBlocks.duringFetal();
            } else if (ppktAge.isCongenital()) {
                return buildBlocks.atBirth();
            } else if (ppktAge.isNeonate()) {
                return buildBlocks.asNeonate();
            } else if (ppktAge.isInfant()) {
                return buildBlocks.asInfant();
            } else if (ppktAge.isChild()) {
                return buildBlocks.inChildhood();
            } else if (ppktAge.isJuvenile()) {
                return buildBlocks.asAdolescent();
            } else if (ppktAge.isYoungAdult()) {
                return buildBlocks.asYoungAdult();
            } else if (ppktAge.isMiddleAge()) {
                return buildBlocks.asMiddleAge();
            } else if (ppktAge.isLateAdultAge()) {
                return buildBlocks.asLateOnset();
            } else if (ppktAge.isAdult()) {
                return buildBlocks.asAdult();
            } else {
                throw new PhenolRuntimeException("Did not recognize onset: " + ppktAge.toString());
            }
        } else {
            throw new PhenolRuntimeException("Bad age type");
        }
    }



}
