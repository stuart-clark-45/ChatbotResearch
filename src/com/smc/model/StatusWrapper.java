package com.smc.model;

import lombok.Getter;
import lombok.Setter;
import twitter4j.Status;

public class StatusWrapper {

  @Getter
  @Setter
  private Status status;

  @Getter
  @Setter
  private boolean fromFeed;
  
  private StatusWrapper() {
    // For Morphia
  }

  public StatusWrapper(Status status){
    this.status = status;
  }

}
