
package de.foellix.aql.converter.droidsafe;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "call-chain",
    "syscall"
})
public class Visibility {

    @JsonProperty("call-chain")
    private Boolean callChain;
    @JsonProperty("syscall")
    private Boolean syscall;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("call-chain")
    public Boolean getCallChain() {
        return callChain;
    }

    @JsonProperty("call-chain")
    public void setCallChain(Boolean callChain) {
        this.callChain = callChain;
    }

    @JsonProperty("syscall")
    public Boolean getSyscall() {
        return syscall;
    }

    @JsonProperty("syscall")
    public void setSyscall(Boolean syscall) {
        this.syscall = syscall;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
