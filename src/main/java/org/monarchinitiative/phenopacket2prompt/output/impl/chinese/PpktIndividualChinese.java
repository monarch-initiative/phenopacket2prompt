package org.monarchinitiative.phenopacket2prompt.output.impl.chinese;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualChinese implements PPKtIndividualInfoGenerator {



    @Override
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
                individualDescription = iso8601individualDescription(psex, isoAge);
            } else if (lastExamAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
                HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) lastExamAge;
                individualDescription = hpoOnsetIndividualDescription(psex, hpoOnsetTermAge);
            } else {
                // should never happen
                throw new PhenolRuntimeException("Did not recognize last exam age type " + lastExamAge.ageType());
            }
        }  else {
            individualDescription =  switch (psex) {
                case FEMALE -> individualDescription = "患者为女性，年龄不详";
                case MALE -> individualDescription = "患者为男性，年龄不详";
                default -> individualDescription = "患者性别和年龄不详";
            };
        }
        if (onsetOpt.isPresent()) {
            var onsetAge = onsetOpt.get();
            if (onsetAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
                Iso8601Age isoAge = (Iso8601Age) onsetAge;
                onsetDescription =  iso8601onsetDescription(isoAge);
            } else if (onsetAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
                HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) onsetAge;
                onsetDescription = hpoOnsetDescription(hpoOnsetTermAge, psex);
            } else {
                // should never happen
                throw new PhenolRuntimeException("Did not recognize last exam age type " + onsetAge.ageType());
            }
        } else {
            onsetDescription = "发病年龄未知";
        }
        return String.format("%s。%s。", individualDescription, onsetDescription);

    }

    private String hpoOnsetDescription(HpoOnsetAge hpoOnsetTermAge, PhenopacketSex psex) {
        return String.format("疾病于患者%s发作",
                nameOfLifeStage(hpoOnsetTermAge, psex));
    }

    
    private String nameOfLifeStage(HpoOnsetAge hpoOnsetTermAge, PhenopacketSex psex) {
        if (hpoOnsetTermAge.isFetus()) {
            return "胎儿时期";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return "出生时";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "婴儿时";
        } else if (hpoOnsetTermAge.isChild()) {
            return "幼年时";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "青少年时期";
        } else if (hpoOnsetTermAge.isNeonate()) {
            return "为新生儿时"; // +bbGenerator.newborn();
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return "青年时" ;
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return "中年时" ;
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return "晚年时" ;
        } else if (hpoOnsetTermAge.isAdult()) {
            // d.h. nicht weiter spezifiziert
            return "成年后" ;
        } else {
            throw new PhenolRuntimeException("Could not identify Chinese life stage name for HpoOnsetAge " + hpoOnsetTermAge.toString());
        }
    }


    private String ymd(Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        List<String> components = new ArrayList<>();
        if (y > 0) {
            components.add(String.format("%d岁", y));
        }
        if (m > 0) {
            components.add(String.format("%d个月", m));
        }
        if (d > 0) {
            components.add(String.format("%d天", d));
        }
        if (components.isEmpty()) {
            return "刚出生";
        } else if (components.size() == 1) {
            return components.get(0);
        } else if (components.size() == 2) {
            return String.format("%s%s大", components.get(0), components.get(1));
        } else {
            // we must have y,m,d
            return String.format("%s%s%s大", components.get(0), components.get(1), components.get(2));
        }
    }

    private String iso8601onsetDescription(Iso8601Age isoAge) {
        return String.format("疾病于患者%s发作", ymd(isoAge));
    }

    private String atIsoAgeExact(PhenopacketAge ppktAge) {
        Iso8601Age iso8601Age = (Iso8601Age) ppktAge;
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        if (y > 10) {
            return String.format("%d岁时", y);
        } else if (y > 0) {
            if (m >= 1) {
                return String.format("%d岁%d个月大时", y, m);
            } else {
                return String.format("%d岁时", y);
            }
        } else if (m>0 && d>0) {
            return String.format("%d个月%d天大时", m, d);
        } else {
            if (m>0 && d==0) {
                return String.format("%d个月大时", m);
            }
            else {
                return String.format("%d天大时", d);
            }
        }
    }


    //TODO delete this method when sure no translation in it is needed
    private String onsetTermAtAgeOf(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return  "胎儿时期";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return  "出生时";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "婴儿时"; // unsure, to be checked
        } else if (hpoOnsetTermAge.isChild()) {
            return "幼年时"; // check
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "青少年时期";
        } else {
            return "成年后";
        }
    }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y>17) {
            return switch (psex) {
                case FEMALE -> String.format("患者为%d岁女性", y);
                case MALE -> String.format("患者为%d岁男性", y);
                default -> String.format("患者%d岁", y);
            };
        } else if (y>9) {
            return switch (psex) {
                case FEMALE -> String.format("患者为%d岁女性青少年", y);
                case MALE -> String.format("患者为%d岁男性青少年", y);
                default -> String.format("患者为%d岁青少年", y);
            };
        } else if (y>0) {
            return switch (psex) {
                case FEMALE -> String.format("患者为一位%s的女童", ymd(iso8601Age));
                case MALE -> String.format("患者为一位%s的男童", ymd(iso8601Age));
                default -> String.format("患者为一位%s的儿童", ymd(iso8601Age));
            };
        } else if (m>0 || d> 0) {
            return switch (psex) {
                case FEMALE -> String.format("患者为一位%s的女婴", ymd(iso8601Age));
                case MALE -> String.format("患者为一位%s的男婴", ymd(iso8601Age));
                default -> String.format("患者为一位%s的婴儿", ymd(iso8601Age));
            };
        } else {
            return switch (psex) {
                case FEMALE -> "患者为一位刚出生的女婴";
                case MALE -> "患者为一位刚出生的男婴";
                default -> "患者为新生儿";
            };
        }
    }

    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> "患者为一名女性胎儿";
                case MALE -> "患者为一名男性胎儿";
                default -> "患者为一名胎儿";
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE ->  "患者为一名女性新生儿";
                case MALE -> "患者为一名男性新生儿";
                default -> "患者为一名新生儿";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE ->  "患者为一名女婴";
                case MALE -> "患者为一名男婴";
                default -> "患者为一名婴儿";
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "患者为一名女童";
                case MALE -> "患者为一名男童";
                default -> "患者为一名儿童";
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "患者为一名女性青少年";
                case MALE -> "患者为一名男性青少年";
                default -> "患者为一名青少年";
            };
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return switch (psex) {
                case FEMALE -> "患者为一位中年女性";
                case MALE -> "患者为一位中年男性";
                default -> "患者为一位中年人";
            };
        }  else if (hpoOnsetTermAge.isYoungAdult()) {
            return switch (psex) {
                case FEMALE -> "患者为一位年轻的成年女性";
                case MALE -> "患者为一位年轻的成年男性";
                default -> "患者为一位年轻的成年人";
            };
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return switch (psex) {
                case FEMALE -> "患者为一位老年女性";
                case MALE -> "患者为一位老年男性";
                default -> "患者为一位老年人";
            };
        } else if (hpoOnsetTermAge.isAdult()) {
            return switch (psex) {
                case FEMALE -> "患者为一名成年女性";
                case MALE -> "患者为一名成年男性";
                default -> "患者为成年人";
            };
        } else {
            throw new PhenolRuntimeException("Did not recognize Chinese HPO Onset term");
        }
    }


    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return "患者";
    }

    @Override
    public String atAgeForVignette(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return atIsoAgeExact(ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            if (ppktAge.isFetus()) {
                return "胎儿时";
            } else if (ppktAge.isCongenital()) {
                return "出生时";
            } else if (ppktAge.isEmbryo()) {
                return "胚胎时期";
            } else if (ppktAge.isNeonate()) {
                return "新生儿时期";
            } else if (ppktAge.isInfant()) {
                return "婴儿时";
            } else if (ppktAge.isChild()) {
                return "童年时";
            } else if (ppktAge.isJuvenile()) {
                return "青少年时期";
            } else if (ppktAge.isYoungAdult()) {
                return "年轻时";
            } else if (ppktAge.isMiddleAge()) {
                return "中年时";
            } else if (ppktAge.isLateAdultAge()) {
                return "晚年时";
            } else if (ppktAge.isAdult()) {
                return "成年时";
            } else {
                throw new PhenolRuntimeException("Did not recognize onset: " + ppktAge.toString());
            }
        } else {
            throw new PhenolRuntimeException("Bad age type");
        }
    }


}
