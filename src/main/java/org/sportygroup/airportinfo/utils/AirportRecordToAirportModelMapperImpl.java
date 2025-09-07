package org.sportygroup.airportinfo.utils;

import java.util.ArrayList;
import java.util.List;
import org.sportygroup.airportinfo.client.aviation.records.Airport;
import org.sportygroup.airportinfo.enums.OWNERSHIP;
import org.sportygroup.airportinfo.enums.STATUS;
import org.sportygroup.airportinfo.enums.TYPE;
import org.sportygroup.airportinfo.enums.USE;
import org.springframework.stereotype.Component;

@Component
public class AirportRecordToAirportModelMapperImpl {

  public static List<org.sportygroup.airportinfo.model.airportquery.response.airport.Airport> map(
      List<Airport> record) {
    if (record == null) {
      return null;
    }

    List<org.sportygroup.airportinfo.model.airportquery.response.airport.Airport> list =
        new ArrayList<org.sportygroup.airportinfo.model.airportquery.response.airport.Airport>(
            record.size());
    for (Airport airport : record) {
      list.add(airportToAirport(airport));
    }

    return list;
  }

  protected static org.sportygroup.airportinfo.model.airportquery.response.airport.Airport
      airportToAirport(Airport airport) {
    if (airport == null) {
      return null;
    }

    org.sportygroup.airportinfo.model.airportquery.response.airport.Airport airport1 =
        new org.sportygroup.airportinfo.model.airportquery.response.airport.Airport();

    airport1.setResponsibleArtcc(airport.responsibleArtcc());
    airport1.setVfrSectional(airport.vfrSectional());
    airport1.setCity(airport.city());
    if (airport.use() != null) {
      airport1.setUse(Enum.valueOf(USE.class, airport.use()));
    }
    airport1.setLatitude(airport.latitude());
    airport1.setCounty(airport.county());
    airport1.setBoundaryArtccName(airport.boundaryArtccName());
    airport1.setControlTower(airport.controlTower());
    if (airport.type() != null) {
      airport1.setType(Enum.valueOf(TYPE.class, airport.type()));
    }
    airport1.setFaaIdent(airport.faaIdent());
    airport1.setLightingSchedule(airport.lightingSchedule());
    airport1.setManagerPhone(airport.managerPhone());
    airport1.setLongitudeSec(airport.longitudeSec());
    airport1.setStateFull(airport.stateFull());
    airport1.setFssPhoneNumerTollfree(airport.fssPhoneNumerTollfree());
    airport1.setEffectiveDate(airport.effectiveDate());
    airport1.setState(airport.state());
    airport1.setLongitude(airport.longitude());
    airport1.setElevation(airport.elevation());
    airport1.setFssPhoneNumber(airport.fssPhoneNumber());
    airport1.setManager(airport.manager());
    airport1.setTpa(airport.tpa());
    airport1.setBoundaryArtcc(airport.boundaryArtcc());
    airport1.setMagneticVariation(airport.magneticVariation());
    airport1.setDistrictOffice(airport.districtOffice());
    airport1.setCustomsAirportOfEntry(airport.customsAirportOfEntry());
    airport1.setBeaconSchedule(airport.beaconSchedule());
    airport1.setLatitudeSec(airport.latitudeSec());
    airport1.setCertificationTypedate(airport.certificationTypedate());
    airport1.setFacilityName(airport.facilityName());
    if (airport.ownership() != null) {
      airport1.setOwnership(Enum.valueOf(OWNERSHIP.class, airport.ownership()));
    }
    airport1.setMilitaryLanding(airport.militaryLanding());
    airport1.setIcaoIdent(airport.icaoIdent());
    airport1.setResponsibleArtccName(airport.responsibleArtccName());
    airport1.setMilitaryJointUse(airport.militaryJointUse());
    airport1.setUnicom(airport.unicom());
    airport1.setCtaf(airport.ctaf());
    airport1.setSiteNumber(airport.siteNumber());
    airport1.setRegion(airport.region());
    airport1.setNotamFacilityIdent(airport.notamFacilityIdent());
    if (airport.status() != null) {
      airport1.setStatus(Enum.valueOf(STATUS.class, airport.status()));
    }

    return airport1;
  }
}
