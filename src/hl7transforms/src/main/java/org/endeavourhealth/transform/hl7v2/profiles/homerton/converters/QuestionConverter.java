package org.endeavourhealth.transform.hl7v2.profiles.homerton.converters;

import org.endeavourhealth.transform.hl7v2.profiles.homerton.datatypes.Zqa;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class QuestionConverter {

    public static List<Questionnaire.QuestionComponent> convert(List<Zqa> questionField) throws TransformException {
        List<Questionnaire.QuestionComponent> questions = new ArrayList<>();

        for (Zqa zqa : questionField)
            if (zqa != null) {
                Questionnaire.QuestionComponent qc = new Questionnaire.QuestionComponent();
                qc.setId(zqa.getQuestionIdentifier());
                qc.setText(zqa.getQuestionLabel());
                qc.setType(convertAnswerTypeCode(zqa.getValueType()));
                questions.add(qc);
            }

        return questions;
    }

    private static Questionnaire.AnswerFormat convertAnswerTypeCode(String answerTypeCode) throws TransformException {
        if (answerTypeCode == null)
            answerTypeCode = "";

        answerTypeCode = answerTypeCode.trim().toLowerCase();

        switch (answerTypeCode) {

            case "i":                                     // integer
                return Questionnaire.AnswerFormat.INTEGER;
            case "d":                                     // datetime
                return Questionnaire.AnswerFormat.DATETIME;
            case "n":                                     // decimal
                return Questionnaire.AnswerFormat.DECIMAL;
            case "o":                                     // open choice
                return Questionnaire.AnswerFormat.OPENCHOICE;
            case "c":                                     // choice
                return Questionnaire.AnswerFormat.CHOICE;
            case "t":                                     // text
                return Questionnaire.AnswerFormat.TEXT;

            default:
                return Questionnaire.AnswerFormat.TEXT;
        }
    }

}
