package org.endeavourhealth.hl7transform.homerton.transforms.legacy;


import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7transform.homerton.parser.zdatatypes.Zqa;
import org.endeavourhealth.hl7transform.homerton.parser.zsegments.ZqaSegment;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;

public class QuestionnaireTransform {

    public static Questionnaire fromHl7v2(ZqaSegment source) throws ParseException, TransformException {
        Questionnaire questionnaire = new Questionnaire();

        //questionnaire.setIdElement(new IdType().setValue(IdentifierHelper.generateId(source.getQuestionnaireId())));
        questionnaire.addIdentifier(new Identifier().setValue(source.getQuestionnaireId()));

        Questionnaire.GroupComponent group = new Questionnaire.GroupComponent();

        if (source.getQuestionAndAnswer() != null)
            for (Questionnaire.QuestionComponent question : convert(source.getQuestionAndAnswer()))
                group.addQuestion(question);

        questionnaire.setGroup(group);

        return questionnaire;
    }

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
