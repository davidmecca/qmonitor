package com.hps.mayo.hl7.utilities;

import org.springframework.stereotype.Component;

@Component
public class Hl7Utilities {

	private String getHl7EventDescription(String eventCode) {

		String eventDescription = null;

		switch (eventCode.toUpperCase().trim()) {

		case "A01":
			eventDescription = "Admit/visit notification";
			break;
		case "A02":
			eventDescription = "Transfer a patient";
			break;
		case "A03":
			eventDescription = "Discharge/end visit";
			break;
		case "A04":
			eventDescription = "Register a patient";
			break;
		case "A05":
			eventDescription = "Pre-admit a patient";
			break;
		case "A06":
			eventDescription = "Change an outpatient to an inpatient";
			break;
		case "A07":
			eventDescription = "Change an inpatient to an outpatient";
			break;
		case "A08":
			eventDescription = "Update patient information";
			break;
		case "A09":
			eventDescription = "Patient departing - tracking";
			break;
		case "A10":
			eventDescription = "Patient arriving - tracking";
			break;
		case "A11":
			eventDescription = "Cancel admit/visit notification";
			break;
		case "A12":
			eventDescription = "Cancel transfer";
			break;
		case "A13":
			eventDescription = "Cancel discharge/end visit";
			break;
		case "A14":
			eventDescription = "Pending admit";
			break;
		case "A15":
			eventDescription = "Pending transfer";
			break;
		case "A16":
			eventDescription = "Pending discharge";
			break;
		case "A17":
			eventDescription = "Swap patients";
			break;
		case "A18":
			eventDescription = "Merge patient information";
			break;
		case "A19":
			eventDescription = "QRY/ADR - Patient query";
			break;
		case "A20":
			eventDescription = "Bed status update";
			break;
		case "A21":
			eventDescription = "Patient goes on a leave of absence";
			break;
		case "A22":
			eventDescription = "Patient returns from a leave of absence";
			break;
		case "A23":
			eventDescription = "Delete a patient record";
			break;
		case "A24":
			eventDescription = "Link patient information";
			break;
		case "A25":
			eventDescription = "Cancel pending discharge";
			break;
		case "A26":
			eventDescription = "Cancel pending transfer";
			break;
		case "A27":
			eventDescription = "Cancel pending admit";
			break;
		case "A28":
			eventDescription = "Add person information";
			break;
		case "A29":
			eventDescription = "Delete person information";
			break;
		case "A30":
			eventDescription = "Merge person information";
			break;
		case "A31":
			eventDescription = "Update person information";
			break;
		case "A32":
			eventDescription = "Cancel patient arriving - tracking";
			break;
		case "A33":
			eventDescription = "Cancel patient departing - tracking";
			break;
		case "A34":
			eventDescription = "Merge patient information - patient";
			break;
		case "A35":
			eventDescription = "Merge patient information - account only";
			break;
		case "A36":
			eventDescription = "Merge patient information - patient ID and account number";
			break;
		case "A37":
			eventDescription = "Unlink patient information";
			break;
		case "A38":
			eventDescription = "Cancel pre-admit";
			break;
		case "A39":
			eventDescription = "Merge person - patient ID";
			break;
		case "A40":
			eventDescription = "Merge patient";
			break;
		case "A41":
			eventDescription = "Merge account - patient account num";
			break;
		case "A42":
			eventDescription = "Merge visit - visit number";
			break;
		case "A43":
			eventDescription = "Move patient information - patient identifier list";
			break;
		case "A44":
			eventDescription = "Move account information - patient account number";
			break;
		case "A45":
			eventDescription = "Move visit information - visit number";
			break;
		case "A46":
			eventDescription = "Change patient ID";
			break;
		case "A47":
			eventDescription = "Change patient identifier list";
			break;
		case "A48":
			eventDescription = "Change alternate patient I";
			break;
		case "A49":
			eventDescription = "Change patient account number";
			break;
		case "A50":
			eventDescription = "Change visit number";
			break;
		case "A51":
			eventDescription = "Change alternate visit ID";
			break;
		case "U40":
			eventDescription = "Unmerge Patient";
			break;
		default:
			eventDescription = "UNKNOWN";

		}
		return eventDescription;
	}

}
