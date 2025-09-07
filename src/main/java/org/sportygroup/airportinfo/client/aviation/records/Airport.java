package org.sportygroup.airportinfo.client.aviation.records;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Record representing an Airport with various attributes. This record is immutable and provides a
 * concise way to store airport information.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Airport(
    String responsibleArtcc,
    String vfrSectional,
    String city,
    String use,
    String latitude,
    String county,
    String boundaryArtccName,
    String controlTower,
    String type,
    String faaIdent,
    String lightingSchedule,
    String managerPhone,
    String longitudeSec,
    String stateFull,
    String fssPhoneNumerTollfree,
    String effectiveDate,
    String state,
    String longitude,
    String elevation,
    String fssPhoneNumber,
    String manager,
    String tpa,
    String boundaryArtcc,
    String magneticVariation,
    String districtOffice,
    String customsAirportOfEntry,
    String beaconSchedule,
    String latitudeSec,
    String certificationTypedate,
    String facilityName,
    String ownership,
    String militaryLanding,
    String icaoIdent,
    String responsibleArtccName,
    String militaryJointUse,
    String unicom,
    String ctaf,
    String siteNumber,
    String region,
    String notamFacilityIdent,
    String status) {

  public Airport(
      String responsibleArtcc,
      String vfrSectional,
      String city,
      String use,
      String latitude,
      String county,
      String boundaryArtccName,
      String controlTower,
      String type,
      String faaIdent,
      String lightingSchedule,
      String managerPhone,
      String longitudeSec,
      String stateFull,
      String fssPhoneNumerTollfree,
      String effectiveDate,
      String state,
      String longitude,
      String elevation,
      String fssPhoneNumber,
      String manager,
      String tpa,
      String boundaryArtcc,
      String magneticVariation,
      String districtOffice,
      String customsAirportOfEntry,
      String beaconSchedule,
      String latitudeSec,
      String certificationTypedate,
      String facilityName,
      String ownership,
      String militaryLanding,
      String icaoIdent,
      String responsibleArtccName,
      String militaryJointUse,
      String unicom,
      String ctaf,
      String siteNumber,
      String region,
      String notamFacilityIdent,
      String status) {
    this.responsibleArtcc = responsibleArtcc;
    this.vfrSectional = vfrSectional;
    this.city = city;
    this.use = use;
    this.latitude = latitude;
    this.county = county;
    this.boundaryArtccName = boundaryArtccName;
    this.controlTower = controlTower;
    this.type = type;
    this.faaIdent = faaIdent;
    this.lightingSchedule = lightingSchedule;
    this.managerPhone = managerPhone;
    this.longitudeSec = longitudeSec;
    this.stateFull = stateFull;
    this.fssPhoneNumerTollfree = fssPhoneNumerTollfree;
    this.effectiveDate = effectiveDate;
    this.state = state;
    this.longitude = longitude;
    this.elevation = elevation;
    this.fssPhoneNumber = fssPhoneNumber;
    this.manager = manager;
    this.tpa = tpa;
    this.boundaryArtcc = boundaryArtcc;
    this.magneticVariation = magneticVariation;
    this.districtOffice = districtOffice;
    this.customsAirportOfEntry = customsAirportOfEntry;
    this.beaconSchedule = beaconSchedule;
    this.latitudeSec = latitudeSec;
    this.certificationTypedate = certificationTypedate;
    this.facilityName = facilityName;
    this.ownership = ownership;
    this.militaryLanding = militaryLanding;
    this.icaoIdent = icaoIdent;
    this.responsibleArtccName = responsibleArtccName;
    this.militaryJointUse = militaryJointUse;
    this.unicom = unicom;
    this.ctaf = ctaf;
    this.siteNumber = siteNumber;
    this.region = region;
    this.notamFacilityIdent = notamFacilityIdent;
    this.status = status;
  }
}
