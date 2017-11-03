package com.smc.model;

import lombok.Getter;
import lombok.Setter;
import twitter4j.Status;

public class StatusWrapper {

  @Getter
  @Setter
  private Status status;
  
  private StatusWrapper() {
    // For Morphia
  }

  public StatusWrapper(Status status){
    this.status = status;
  }

}
