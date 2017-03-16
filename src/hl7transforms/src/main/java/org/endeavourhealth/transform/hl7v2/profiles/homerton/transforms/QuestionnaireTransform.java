package org.endeavourhealth.transform.hl7v2.profiles.homerton.transforms;


import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.converters.QuestionConverter;
import org.endeavourhealth.transform.hl7v2.profiles.homerton.segments.ZqaSegment;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.*;

public class QuestionnaireTransform {

    public static Questionnaire fromHl7v2(ZqaSegment source) throws ParseException, TransformException {
        Questionnaire questionnaire = new Questionnaire();

        //questionnaire.setIdElement(new IdType().setValue(IdentifierHelper.generateId(source.getQuestionnaireId())));
        questionnaire.addIdentifier(new Identifier().setValue(source.getQuestionnaireId()));

        Questionnaire.GroupComponent group = new Questionnaire.GroupComponent();

        if (source.getQuestionAndAnswer() != null)
            for (Questionnaire.QuestionComponent question : QuestionConverter.convert(source.getQuestionAndAnswer()))
                group.addQuestion(question);

        questionnaire.setGroup(group);

        return questionnaire;
    }
}
