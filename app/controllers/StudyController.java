package controllers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.bridge.config.BridgeConfigFactory;
import org.sagebionetworks.bridge.dynamodb.DynamoStudy;
import org.sagebionetworks.bridge.models.VersionHolder;
import org.sagebionetworks.bridge.models.accounts.UserSession;
import org.sagebionetworks.bridge.models.studies.Study;
import org.sagebionetworks.bridge.models.studies.StudyIdentifier;
import org.sagebionetworks.bridge.models.studies.StudyInfo;
import org.sagebionetworks.bridge.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import play.libs.Json;
import play.mvc.Result;

@Controller("studyController")
public class StudyController extends BaseController {

    private final Set<String> studyWhitelist = Collections.unmodifiableSet(new HashSet<>(
            BridgeConfigFactory.getConfig().getPropertyAsList("study.whitelist")));

    private UserProfileService userProfileService;

    @Autowired
    public void setUserProfileService(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    public Result getStudyForResearcher() throws Exception {
        UserSession session = getAuthenticatedResearcherSession();
        Study study = studyService.getStudy(session.getStudyIdentifier());
        return okResult(new StudyInfo(study));
    }
    
    public Result sendStudyParticipantsRoster() throws Exception {
        // Researchers only, administrators cannot get this list so easily
        UserSession session = getAuthenticatedResearcherSession();
        Study study = studyService.getStudy(session.getStudyIdentifier());
        
        userProfileService.sendStudyParticipantRoster(study);
        return okResult("A roster of study participants will be emailed to the study's consent notification contact.");
    }

    public Result updateStudyForResearcher() throws Exception {
        UserSession session = getAuthenticatedResearcherSession();
        StudyIdentifier studyId = session.getStudyIdentifier();

        Study studyUpdate = DynamoStudy.fromJson(requestToJSON(request()));
        studyUpdate.setIdentifier(studyId.getIdentifier());
        studyUpdate = studyService.updateStudy(studyUpdate);
        return okResult(new VersionHolder(studyUpdate.getVersion()));
    }

    public Result updateStudy(String identifier) throws Exception {
        getAuthenticatedAdminSession();

        Study studyUpdate = DynamoStudy.fromJson(requestToJSON(request()));
        studyUpdate = studyService.updateStudy(studyUpdate);
        return okResult(new VersionHolder(studyUpdate.getVersion()));
    }

    public Result getStudy(String identifier) throws Exception {
        getAuthenticatedAdminSession();

        Study study = studyService.getStudy(identifier);
        return okResult(new StudyInfo(study));
    }

    public Result getAllStudies() throws Exception {
        getAuthenticatedAdminSession();

        List<StudyInfo> studies = Lists.transform(studyService.getStudies(), new Function<Study,StudyInfo>() {
            @Override
            public StudyInfo apply(Study study) {
                return new StudyInfo(study);
            }
        });
        return okResult(studies);
    }

    public Result createStudy() throws Exception {
        getAuthenticatedAdminSession();

        Study study = DynamoStudy.fromJson(requestToJSON(request()));
        study = studyService.createStudy(study);
        return okResult(new VersionHolder(study.getVersion()));
    }

    public Result deleteStudy(String identifier) throws Exception {
        getAuthenticatedAdminSession();
        if (studyWhitelist.contains(identifier)) {
            return forbidden(Json.toJson(identifier + " is protected by whitelist."));
        }
        studyService.deleteStudy(identifier);
        return okResult("Study deleted.");
    }
}
