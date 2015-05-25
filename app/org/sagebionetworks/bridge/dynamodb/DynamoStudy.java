package org.sagebionetworks.bridge.dynamodb;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.sagebionetworks.bridge.exceptions.BridgeServiceException;
import org.sagebionetworks.bridge.json.BridgeTypeName;
import org.sagebionetworks.bridge.json.JsonUtils;
import org.sagebionetworks.bridge.models.studies.Study;
import org.sagebionetworks.bridge.models.studies.StudyIdentifier;
import org.sagebionetworks.bridge.models.studies.StudyIdentifierImpl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@DynamoDBTable(tableName = "Study")
@BridgeTypeName("Study")
public class DynamoStudy implements Study {

    // We need the unconfigured mapper for setData/getData.
    private static ObjectMapper mapper = new ObjectMapper();

    private static final String IDENTIFIER_PROPERTY = "identifier";
    private static final String NAME_PROPERTY = "name";
    private static final String MAX_NUM_OF_PARTICIPANTS_PROPERTY = "maxNumOfParticipants";
    private static final String MIN_AGE_OF_CONSENT_PROPERTY = "minAgeOfConsent";
    private static final String RESEARCHER_ROLE_PROPERTY = "researcherRole";
    private static final String STORMPATH_HREF_PROPERTY = "stormpathHref";
    private static final String VERSION_PROPERTY = "version";
    private static final String SUPPORT_EMAIL_PROPERTY = "supportEmail";
    private static final String CONSENT_NOTIFICATION_EMAIL_PROPERTY = "consentNotificationEmail";
    private static final String USER_PROFILE_ATTRIBUTES_PROPERTY = "userProfileAttributes";
    
    private String name;
    private String identifier;
    private String researcherRole;
    private String stormpathHref;
    private String supportEmail;
    private String consentNotificationEmail;
    private int minAgeOfConsent;
    private int maxNumOfParticipants;
    private Long version;
    private StudyIdentifier studyIdentifier;
    private Set<String> profileAttributes;

    public static DynamoStudy fromJson(JsonNode node) {
        DynamoStudy study = new DynamoStudy();
        study.setIdentifier(JsonUtils.asText(node, IDENTIFIER_PROPERTY));
        study.setName(JsonUtils.asText(node, NAME_PROPERTY));
        study.setMinAgeOfConsent(JsonUtils.asInt(node, MIN_AGE_OF_CONSENT_PROPERTY));
        study.setMaxNumOfParticipants(JsonUtils.asInt(node, MAX_NUM_OF_PARTICIPANTS_PROPERTY));
        study.setVersion(JsonUtils.asLong(node, VERSION_PROPERTY));
        study.setSupportEmail(JsonUtils.asText(node, SUPPORT_EMAIL_PROPERTY));
        study.setConsentNotificationEmail(JsonUtils.asText(node, CONSENT_NOTIFICATION_EMAIL_PROPERTY));
        study.setUserProfileAttributes(JsonUtils.asStringSet(node, USER_PROFILE_ATTRIBUTES_PROPERTY));
        return study;
    }

    public static DynamoStudy fromCacheJson(JsonNode node) {
        DynamoStudy study = fromJson(node);
        study.setStormpathHref(JsonUtils.asText(node, STORMPATH_HREF_PROPERTY));
        study.setResearcherRole(JsonUtils.asText(node, RESEARCHER_ROLE_PROPERTY));
        return study;
    }
    
    public DynamoStudy() {
        profileAttributes = new HashSet<>();
    }
    
    @Override
    @DynamoDBAttribute
    public String getName() {
        return name;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }
    @Override
    @DynamoDBHashKey
    public String getIdentifier() {
        return identifier;
    }
    @Override
    public void setIdentifier(String identifier) {
        if (identifier != null) {
            this.identifier = identifier;
            this.studyIdentifier = new StudyIdentifierImpl(identifier);
        }
    }
    @Override
    @JsonIgnore
    @DynamoDBIgnore
    public StudyIdentifier getStudyIdentifier() {
        return studyIdentifier;
    }
    @Override
    @DynamoDBVersionAttribute
    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }
    @DynamoDBIgnore
    @Override
    public String getResearcherRole() {
        return researcherRole;
    }
    @Override
    public void setResearcherRole(String role) {
        this.researcherRole = role;
    }
    @DynamoDBIgnore
    @Override
    public int getMinAgeOfConsent() {
        return minAgeOfConsent;
    }
    @Override
    public void setMinAgeOfConsent(int minAge) {
        this.minAgeOfConsent = minAge;
    }
    @DynamoDBIgnore
    @Override
    public int getMaxNumOfParticipants() {
        return maxNumOfParticipants;
    }
    @Override
    public void setMaxNumOfParticipants(int maxParticipants) {
        this.maxNumOfParticipants = maxParticipants;
    }
    @DynamoDBIgnore
    @Override
    public String getStormpathHref() {
        return stormpathHref;
    }
    @Override
    public void setStormpathHref(String stormpathHref) {
        this.stormpathHref = stormpathHref;
    }
    /**
     * A comma-separated list of email addresses that should be used to send technical 
     * support email to the research team from the application (optional).
     */
    @DynamoDBIgnore
    @Override
    public String getSupportEmail() {
        return supportEmail;
    }
    @Override
    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }
    /**
     * A comma-separated list of email addresses that should be sent consent records 
     * when a user agrees to participate in research (optional, but should be provided 
     * for active studies).
     */
    @DynamoDBIgnore
    @Override
    public String getConsentNotificationEmail() {
        return consentNotificationEmail;
    }
    @Override
    public void setConsentNotificationEmail(String consentNotificationEmail) {
        this.consentNotificationEmail = consentNotificationEmail;
    }
    @DynamoDBIgnore
    @Override
    public Set<String> getUserProfileAttributes() {
        return profileAttributes;
    }
    @Override
    public void setUserProfileAttributes(Set<String> profileAttributes) {
        this.profileAttributes = profileAttributes;
    }
    
    @DynamoDBAttribute
    @JsonIgnore
    public String getData() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put(RESEARCHER_ROLE_PROPERTY, researcherRole);
        node.put(MIN_AGE_OF_CONSENT_PROPERTY, minAgeOfConsent);
        node.put(MAX_NUM_OF_PARTICIPANTS_PROPERTY, maxNumOfParticipants);
        node.put(STORMPATH_HREF_PROPERTY, stormpathHref);
        node.put(SUPPORT_EMAIL_PROPERTY, supportEmail);
        node.put(CONSENT_NOTIFICATION_EMAIL_PROPERTY, consentNotificationEmail);
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        if (profileAttributes != null) {
            for (String att : profileAttributes) {
                array.add(att);
            }
        }
        node.set(USER_PROFILE_ATTRIBUTES_PROPERTY, array);    
        return node.toString();
    }
    public void setData(String data) {
        try {
            JsonNode node = mapper.readTree(data);
            this.researcherRole = JsonUtils.asText(node, RESEARCHER_ROLE_PROPERTY);
            this.minAgeOfConsent = JsonUtils.asIntPrimitive(node, MIN_AGE_OF_CONSENT_PROPERTY);
            this.maxNumOfParticipants = JsonUtils.asIntPrimitive(node, MAX_NUM_OF_PARTICIPANTS_PROPERTY);
            this.supportEmail = JsonUtils.asText(node, SUPPORT_EMAIL_PROPERTY);
            this.consentNotificationEmail = JsonUtils.asText(node, CONSENT_NOTIFICATION_EMAIL_PROPERTY);
            this.stormpathHref = JsonUtils.asText(node, STORMPATH_HREF_PROPERTY);
            this.profileAttributes = JsonUtils.asStringSet(node, USER_PROFILE_ATTRIBUTES_PROPERTY);
        } catch (IOException e) {
            throw new BridgeServiceException(e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + maxNumOfParticipants;
        result = prime * result + minAgeOfConsent;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((researcherRole == null) ? 0 : researcherRole.hashCode());
        result = prime * result + ((supportEmail == null) ? 0 : supportEmail.hashCode());
        result = prime * result + ((consentNotificationEmail == null) ? 0 : consentNotificationEmail.hashCode());
        result = prime * result + ((stormpathHref == null) ? 0 : stormpathHref.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((profileAttributes == null) ? 0 : profileAttributes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DynamoStudy other = (DynamoStudy) obj;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (maxNumOfParticipants != other.maxNumOfParticipants)
            return false;
        if (minAgeOfConsent != other.minAgeOfConsent)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (researcherRole == null) {
            if (other.researcherRole != null)
                return false;
        } else if (!researcherRole.equals(other.researcherRole))
            return false;
        if (stormpathHref == null) {
            if (other.stormpathHref != null)
                return false;
        } else if (!stormpathHref.equals(other.stormpathHref))
            return false;
        if (supportEmail == null) {
            if (other.supportEmail != null)
                return false;
        } else if (!supportEmail.equals(other.supportEmail))
            return false;
        if (consentNotificationEmail == null) {
            if (other.consentNotificationEmail != null)
                return false;
        } else if (!consentNotificationEmail.equals(other.consentNotificationEmail))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        if (profileAttributes == null) {
            if (other.profileAttributes != null)
                return false;
        } else if (!profileAttributes.equals(other.profileAttributes))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DynamoStudy [name=" + name + ", identifier=" + identifier + ", researcherRole=" + researcherRole
            + ", stormpathHref=" + stormpathHref + ", minAgeOfConsent=" + minAgeOfConsent
            + ", maxNumOfParticipants=" + maxNumOfParticipants + ", supportEmail=" + supportEmail
            + ", consentNotificationEmail=" + consentNotificationEmail + ", version=" + version
            + ", userProfileAttributes=" + profileAttributes + "]";
    }
}
