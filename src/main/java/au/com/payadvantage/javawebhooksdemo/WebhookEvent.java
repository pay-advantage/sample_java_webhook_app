package au.com.payadvantage.javawebhooksdemo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebhookEvent {
    @JsonProperty(required = true)
    public String code;

    @JsonProperty(required = true)
    public String merchantCode;

    public String dateCreated;

    public String dateUpdated;

    @JsonProperty(required = true)
    public String event;

    @JsonProperty(required = true)
    public String resourceCode;

    public String endpointCode;

    public String endpointUrl;

    public String resourceUrl;

    @JsonProperty(required = true)
    public String status;
}
