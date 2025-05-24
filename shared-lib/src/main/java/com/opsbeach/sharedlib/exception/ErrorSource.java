package com.opsbeach.sharedlib.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.shaded.json.JSONObject;

public class ErrorSource {

    @JsonProperty("error")
    private Error error;

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public static class Error {
        @JsonProperty("message")
        private String message;
        @JsonProperty("type")
        private String type;
        @JsonProperty("code")
        private String code;
        @JsonProperty("decline_code")
        private String declineCode;
        @JsonProperty("param")
        private String param;
        @JsonProperty("doc_url")
        private String docUrl;
        @JsonProperty("charge")
        private String charge;
        @JsonProperty("payment_intent")
        private JSONObject paymentIntent;
        @JsonProperty("payment_method")
        private JSONObject paymentMethod;
        @JsonProperty("source")
        private String source;
        @JsonProperty("setup_intent")
        private JSONObject setupIntent;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDeclineCode() {
            return declineCode;
        }

        public void setDeclineCode(String declineCode) {
            this.declineCode = declineCode;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }

        public String getDocUrl() {
            return docUrl;
        }

        public void setDocUrl(String docUrl) {
            this.docUrl = docUrl;
        }

        public String getCharge() {
            return charge;
        }

        public void setCharge(String charge) {
            this.charge = charge;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public JSONObject getPaymentIntent() {
            return paymentIntent;
        }

        public void setPaymentIntent(JSONObject paymentIntent) {
            this.paymentIntent = paymentIntent;
        }

        public JSONObject getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(JSONObject paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public JSONObject getSetupIntent() {
            return setupIntent;
        }

        public void setSetupIntent(JSONObject setupIntent) {
            this.setupIntent = setupIntent;
        }
    }
}