package com.hps.mayo.businessobjects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.v2xml.CX;
import org.hl7.v2xml.EVNCONTENT;
import org.hl7.v2xml.MRGCONTENT;
import org.hl7.v2xml.PIDCONTENT;
import org.hl7.v2xml.PV1CONTENT;
import org.hl7.v2xml.TS;
import org.hl7.v2xml.ZSFCONTENT;

import com.google.common.base.Strings;
import com.hps.mayo.configuration.ApplicationConfiguration;
import com.hps.mayo.exceptions.MessageProcessingException;

import edu.mayo.mdm.party.services.partyservicedata.AlternateIdValue;
import edu.mayo.mdm.party.services.partyservicedata.Party;
import edu.mayo.mdm.party.services.partyservicedata.PartyNames;
import edu.mayo.mdm.party.services.partyservicedata.PartyObject;
import edu.mayo.mdm.party.services.partyservicedata.PartyRole;
import edu.mayo.mdm.party.services.partyservicedata.RoleAddr;
import edu.mayo.mdm.party.services.partyservicedata.RoleAlternateId;
import edu.mayo.mdm.party.services.partyservicedata.RoleAttr;
import edu.mayo.mdm.party.services.partyservicedata.RoleComm;
import edu.mayo.mdm.party.services.partyservicedata.UserCredentials;
import edu.mayo.mdm.party.services.partyservicemessages.MergePartyRequest;
import edu.mayo.mdm.party.services.partyservicemessages.MergePartyResponse;
import edu.mayo.mdm.party.services.partyservicemessages.PutPartyRequest;
import edu.mayo.mdm.party.services.partyservicemessages.PutPartyResponse;
import edu.mayo.mdm.party.services.partyservicemessages.SearchPartyRequest;
import edu.mayo.mdm.party.services.partyservicemessages.SearchPartyResponse;
import edu.mayo.mdm.party.services.partyservicemessages.UnmergePartyRequest;
import edu.mayo.mdm.party.services.partyservicemessages.UnmergePartyResponse;
import edu.mayo.mdm.util.AppProperties;
import edu.mayo.mdm.util.Constants;
import edu.mayo.mdm.util.HL7Segments;
import edu.mayo.model.dao.MdmPersonDao;

public class MdmInterfaceMessage {

	private static final Logger logger = LogManager.getLogger(MdmInterfaceMessage.class);

	private static ApplicationConfiguration appConfig;

	public static String nl = "\n";
	private static final Properties configProperties = AppProperties.getInstance().getConfigProperties();
	private SearchPartyRequest searchPartyRequest;
	private SearchPartyResponse searchPartyResponse;
	private PutPartyRequest putPartyRequest;
	private PutPartyResponse putPartyResponse;
	private MergePartyRequest mergePartyRequest;
	private MergePartyResponse mergePartyResponse;
	private UnmergePartyRequest unmergePartyRequest;
	private UnmergePartyResponse unmergePartyResponse;
	private HL7Segments hl7Segments;

	private static final String sourceSystemName = "MPII";
	private static final String mcTypeCode = "MC";
	private static final String partyRoleType = "PAT";
	private static final String partyAddressTypeHome = "HOM";
	private static final String partyCommunicationTypePhone = "PHN";
	private static final String partyCommunicationUsageType = "HOM";
	private static final String ssnTypeCode = "SSN";
	private static final String DoubleQuoteString = "\"\"";

	public MdmInterfaceMessage() {
	}

	public MdmInterfaceMessage(ApplicationConfiguration appConfig) {
		this.appConfig = appConfig;
	}

	// constructor with HL7 message
	public MdmInterfaceMessage(HL7Segments _hl7Segments) {
		this.hl7Segments = _hl7Segments;
	}

	public MdmInterfaceMessage(SearchPartyRequest searchPartyRequest) {
		this.searchPartyRequest = searchPartyRequest;
		this.searchPartyResponse = new SearchPartyResponse();
	}

	public static String getSourceSystemName() {
		return sourceSystemName;
	}

	public static String getMCTypeCode() {
		return mcTypeCode;
	}

	public static String getPartyroletype() {
		return partyRoleType;
	}

	public SearchPartyRequest getSearchPartyRequest() {
		return searchPartyRequest;
	}

	public void setSearchPartyRequest(SearchPartyRequest searchPartyRequest) {
		this.searchPartyRequest = searchPartyRequest;
	}

	public SearchPartyResponse getSearchPartyResponse() {
		return searchPartyResponse;
	}

	public void setSearchPartyResponse(SearchPartyResponse searchPartyResponse) {
		this.searchPartyResponse = searchPartyResponse;
	}

	public PutPartyRequest getPutPartyRequest(boolean nullOutMissingFields) throws Exception {
		String outMsg = null;

		if (logger.isDebugEnabled()) {
			outMsg = "-------- BEGIN mdmPerson.getPutPartyRequest --------";
			logger.debug(outMsg);
		}

		if (putPartyRequest == null) {
			putPartyRequest = new PutPartyRequest();
		}
		putPartyRequest.setNullOutMissingFields(nullOutMissingFields);
		PartyObject partyObject = new PartyObject();
		Party party = MdmInterfaceMessage.parseMessage(hl7Segments);
		partyObject.setParty(party);
		partyObject.setPartyRole(MdmInterfaceMessage.parsePartyRole(hl7Segments, party));
		partyObject.setPartyNames(MdmInterfaceMessage.parsePartyNames(hl7Segments, party));
		putPartyRequest.setPartyObject(partyObject);

		if (logger.isDebugEnabled()) {
			outMsg = "-------- END mdmPerson.getPutPartyRequest --------";
			logger.debug(outMsg);
		}

		return putPartyRequest;
	}

	public void setPutPartyRequest(PutPartyRequest putPartyRequest) {
		this.putPartyRequest = putPartyRequest;
	}

	public MergePartyRequest getMergePartyRequest() throws Exception {

		String outMsg = null;

		if (mergePartyRequest == null) {
			mergePartyRequest = new MergePartyRequest();
		}

		if (logger.isDebugEnabled()) {
			outMsg = nl + "-------- BEGIN mdmPerson.getMergePartyRequest --------" + nl;
			logger.debug(outMsg);
		}

		try {
			// parse party (The person who will be merged into another one)
			Party party = MdmInterfaceMessage.parseMessage(hl7Segments);

			mergePartyRequest.setMergeToId(party.getSourceSystemId());
			mergePartyRequest.setMergeToSourceSystem(MdmInterfaceMessage.getSourceSystemName());

			// parse prior party (The person who will remain)
			Party priorParty = MdmInterfaceMessage.parsePriorParty(hl7Segments);

			mergePartyRequest.setMergeFromId(priorParty.getSourceSystemId());
			mergePartyRequest.setMergeFromSourceSystem(MdmInterfaceMessage.getSourceSystemName());

			/*
			 * Allow merge of same source and target to process so a valid SIP
			 * error is logged and returned if (party != null && priorParty !=
			 * null) {
			 * 
			 */

		} catch (Exception e) {
			String msg = "Exception in getMergePartyRequest.  Exception: " + e;
			logger.error(msg);
			throw new MessageProcessingException(msg);
		}

		if (logger.isDebugEnabled()) {
			outMsg = "-------- END mdmPerson.getMergePartyRequest --------";
			logger.debug(outMsg);
		}

		return mergePartyRequest;
	}

	public void setMergePartyRequest(MergePartyRequest mergePartyRequest) {
		this.mergePartyRequest = mergePartyRequest;
	}

	public MergePartyResponse getMergePartyResponse() {
		return mergePartyResponse;
	}

	public void setMergePartyResponse(MergePartyResponse mergePartyResponse) {
		this.mergePartyResponse = mergePartyResponse;
	}

	public UnmergePartyRequest getUnmergePartyRequest() throws Exception {
		String outMsg = "";

		if (unmergePartyRequest == null) {
			unmergePartyRequest = new UnmergePartyRequest();
		}

		if (logger.isDebugEnabled()) {
			outMsg = "-------- BEGIN mdmPerson.getUnmergePartyRequest --------";
			logger.debug(outMsg);
		}

		// parse party (The person who will be unmerged from the another one)
		Party party = MdmInterfaceMessage.parsePriorParty(hl7Segments);

		unmergePartyRequest.setMergeFromId(party.getSourceSystemId());
		unmergePartyRequest.setMergeFromSourceSystem(party.getSourceSystemName());

		if (logger.isDebugEnabled()) {
			outMsg = "-------- END mdmPerson.getUnmergePartyRequest --------";
			logger.debug(outMsg);
		}

		return unmergePartyRequest;
	}

	public void setUnmergePartyRequest(UnmergePartyRequest unmergePartyRequest) {
		this.unmergePartyRequest = unmergePartyRequest;
	}

	public UnmergePartyResponse getUnmergePartyResponse() {
		return unmergePartyResponse;
	}

	public void setUnmergePartyResponse(UnmergePartyResponse unmergePartyResponse) {
		this.unmergePartyResponse = unmergePartyResponse;
	}

	// END UNMERGE

	public PutPartyResponse getPutPartyResponse() {
		return putPartyResponse;
	}

	public void setPutPartyResponse(PutPartyResponse putPartyResponse) {
		this.putPartyResponse = putPartyResponse;
	}

	/**
	 * Parse the HL7 and create Party object.
	 * 
	 * @return Party object
	 * 
	 */
	public static Party parseMessage(HL7Segments hl7Segments) throws Exception {

		Party party = new Party();
		PIDCONTENT pid = hl7Segments.getPid();
		EVNCONTENT evn = hl7Segments.getEvn();

		String outMsg = "";

		if (logger.isDebugEnabled()) {
			outMsg = nl + "-------- BEGIN MdmPerson.parseParty --------" + nl;
			logger.debug(outMsg);
		}

		party.setSourceSystemName(sourceSystemName); // Source system name
														// defaulted to MPII
		String mpiiNumber = "";
		String mcTypeCodeIncoming = "";

		try {

			// Extract MPII number
			if (pid.getPID2PatientID() != null && pid.getPID2PatientID().getCX1() != null) {
				mpiiNumber = pid.getPID2PatientID().getCX1().trim();
				if (logger.isDebugEnabled()) {
					outMsg = "mpiiNumber" + Constants.LOG_FILE_DELIMITER + "pid.getPID2PatientID().getCX1()"
							+ Constants.LOG_FILE_DELIMITER + mpiiNumber + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
				if (mpiiNumber == null || mpiiNumber.trim().length() == 0) {
					String msg = "Null/empty value for MPII number in message element PID.2.PatientID.CX.1";
					logger.error(msg);
					throw new MessageProcessingException(msg);
				}
			}

			String mcNumber = "";
			// Extract MC code
			if (pid.getPID3PatientIdentifierList() != null && pid.getPID3PatientIdentifierList().size() > 0) {
				for (int i = 0; i < pid.getPID3PatientIdentifierList().size(); i++) {
					mcTypeCodeIncoming = pid.getPID3PatientIdentifierList().get(i).getCX5();
					if (logger.isDebugEnabled()) {
						outMsg = "mcTypeCodeIncoming" + Constants.LOG_FILE_DELIMITER
								+ "pid.getPID3PatientIdentifierList().get(i).getCX5()" + Constants.LOG_FILE_DELIMITER
								+ mcTypeCodeIncoming + Constants.LOG_FILE_DELIMITER;
						logger.debug(outMsg);
					}
					if (mcTypeCodeIncoming != null && mcTypeCodeIncoming.trim().length() > 0) {
						if (mcTypeCodeIncoming.equals(mcTypeCode)) {
							mcNumber = pid.getPID3PatientIdentifierList().get(0).getCX1();
							if (mcNumber == null || mcNumber.trim().length() == 0) {
								String msg = "Null/empty value for MC code value in message element PID.3.PatientIdentifierList.CX.1 for MPII number: "
										+ mpiiNumber;
								logger.error(msg);
								throw new MessageProcessingException(msg);
							} else {
								mcNumber = mcNumber.trim();
							}
							if (logger.isDebugEnabled()) {
								outMsg = "mcNumber" + Constants.LOG_FILE_DELIMITER
										+ "pid.getPID3PatientIdentifierList().get(0).getCX1()"
										+ Constants.LOG_FILE_DELIMITER + mcNumber + Constants.LOG_FILE_DELIMITER;
								logger.debug(outMsg);
							}
						}
					} else {
						String msg = "Null/empty value for MC type code value in message element PID.3.PatientIdentifierList.CX.5 for MPII number: "
								+ mpiiNumber;
						logger.error(msg);
						throw new MessageProcessingException(msg);
					}
				}
			}

			if (!(isNullOrEmpty(mcNumber) || isDoubleQuotes(mcNumber))) {
				if (logger.isDebugEnabled()) {
					outMsg = "setSourceSystemName" + Constants.LOG_FILE_DELIMITER + "mcTypeCodeIncoming"
							+ Constants.LOG_FILE_DELIMITER + mcTypeCodeIncoming + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				String sourceSystemNumberPrefix = appConfig.getSourceSystemPrefix();
				logger.debug(
						"Checking for Source System static testing prefix value.  Found:" + sourceSystemNumberPrefix);
				if (sourceSystemNumberPrefix != null && sourceSystemNumberPrefix.trim().length() > 0) {

					mcNumber = sourceSystemNumberPrefix + mcNumber;
					logger.debug("Event code: " + hl7Segments.getEvn().getEVN1EventTypeCode()
							+ " -- Setting static prefix: " + sourceSystemNumberPrefix + " for MC number: " + mcNumber);

				}

				party.setSourceSystemId(mcNumber);
				if (logger.isDebugEnabled()) {
					outMsg = "setSourceSystemId" + Constants.LOG_FILE_DELIMITER + "mcNumber"
							+ Constants.LOG_FILE_DELIMITER + mcNumber + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
			}

			// Extract Patient's full name
			if (pid.getPID5PatientName() != null && pid.getPID5PatientName().size() > 0) {

				// Extract First Name
				String pid_firstName = null;
				if (pid.getPID5PatientName().get(0).getXPN2() != null) {
					pid_firstName = pid.getPID5PatientName().get(0).getXPN2();
				}
				party.setFirstName(pid_firstName);
				if (logger.isDebugEnabled()) {
					outMsg = "pid_firstName" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID5PatientName().get(0).getXPN2()" + Constants.LOG_FILE_DELIMITER + pid_firstName
							+ Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				// Extract Last Name
				String pid_lastName = null;
				if (pid.getPID5PatientName().get(0).getXPN1() != null) {
					pid_lastName = pid.getPID5PatientName().get(0).getXPN1().getFN1();
				}
				party.setLastName(pid_lastName);
				if (logger.isDebugEnabled()) {
					outMsg = "pid_lastName" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID5PatientName().get(0).getXPN1().getFN1()" + Constants.LOG_FILE_DELIMITER
							+ pid_lastName + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
				// Extract Middle Name
				String pid_middleName = null;
				if (pid.getPID5PatientName().get(0).getXPN3() != null) {
					pid_middleName = pid.getPID5PatientName().get(0).getXPN3();
				}
				party.setMiddleName(pid_middleName);
				if (logger.isDebugEnabled()) {
					outMsg = "pid_middleName" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID5PatientName().get(0).getXPN3()" + Constants.LOG_FILE_DELIMITER
							+ pid_middleName + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				// Extract Suffix
				String pid_suffixName = null;
				if (pid.getPID5PatientName().get(0).getXPN4() != null) {
					pid_suffixName = pid.getPID5PatientName().get(0).getXPN4();
				}
				party.setSuffixName(pid_suffixName);
				if (logger.isDebugEnabled()) {
					outMsg = "pid_suffixName" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID5PatientName().get(0).getXPN4()" + Constants.LOG_FILE_DELIMITER
							+ pid_suffixName + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				// Extract Prefix
				String pid_prefixName = null;
				if (pid.getPID5PatientName().get(0).getXPN5() != null) {
					pid_prefixName = pid.getPID5PatientName().get(0).getXPN5();
				}
				party.setPrefixNameCode(pid_prefixName);
				if (logger.isDebugEnabled()) {
					outMsg = "pid_prefixName" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID5PatientName().get(0).getXPN5()" + Constants.LOG_FILE_DELIMITER
							+ pid_prefixName + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

			} else {
				party.setFirstName(null);
				party.setLastName(null);
				party.setMiddleName(null);
				party.setSuffixName(null);
				party.setPrefixNameCode(null);
			}

			// Extract date of birth
			String pid_DateTimeOfBirth = null;
			if (pid.getPID7DateTimeOfBirth() != null && isNullOrEmpty(pid.getPID7DateTimeOfBirth().getTS1()) == false) {
				pid_DateTimeOfBirth = pid.getPID7DateTimeOfBirth().getTS1();
				pid_DateTimeOfBirth = formatHL7DateTimeToDateTime(pid_DateTimeOfBirth);
			}
			party.setBirthDateTime(pid_DateTimeOfBirth);
			if (logger.isDebugEnabled()) {
				outMsg = "pid_DateTimeOfBirth" + Constants.LOG_FILE_DELIMITER
						+ "pid.getPID7DateTimeOfBirth().getTS1(): " + Constants.LOG_FILE_DELIMITER + pid_DateTimeOfBirth
						+ Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			// Extract date of death
			TS deathTS = pid.getPID29PatientDeathDateandTime();
			String deathTSString = null;
			if (deathTS != null) {
				deathTSString = deathTS.getTS1();
				if (isNullOrEmpty(deathTSString) == false) {
					deathTSString = formatHL7DateTimeToDateTime(deathTSString);
				}
			}
			party.setDeathDateTime(deathTSString);
			if (logger.isDebugEnabled()) {
				outMsg = "pid_DateTimeOfDeath" + Constants.LOG_FILE_DELIMITER
						+ "pid.getPID29PatientDeathDateandTime().getTS1(): " + Constants.LOG_FILE_DELIMITER
						+ deathTSString + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			// Extract date of death indicator
			String pid_DeathIndicator = null;
			if (isNullOrEmpty(pid.getPID30PatientDeathIndicator()) == false) {
				pid_DeathIndicator = pid.getPID30PatientDeathIndicator();
			}
			party.setDeathIndicator(pid_DeathIndicator);
			if (logger.isDebugEnabled()) {
				outMsg = "pid_DeathIndicator" + Constants.LOG_FILE_DELIMITER + "pid.getPID30PatientDeathIndicator()"
						+ Constants.LOG_FILE_DELIMITER + pid_DeathIndicator + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			// Extract ethnic code
			String pid_ethnicCode = null;
			if (pid.getPID22EthnicGroup() != null && pid.getPID22EthnicGroup().size() > 0) {
				pid_ethnicCode = pid.getPID22EthnicGroup().get(0).getCE1();
			}
			party.setEthnicCode(pid_ethnicCode);
			if (logger.isDebugEnabled()) {
				outMsg = "pid_ethnicCode" + Constants.LOG_FILE_DELIMITER + "pid.getPID22EthnicGroup().get(0).getCE1()"
						+ Constants.LOG_FILE_DELIMITER + pid_ethnicCode + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			// Extract primary language
			String pid_languageCodeISO3 = null;
			if (pid.getPID15PrimaryLanguage() != null) {
				pid_languageCodeISO3 = pid.getPID15PrimaryLanguage().getCE1();
			}
			party.setLanguageCodeISO3(pid_languageCodeISO3);
			if (logger.isDebugEnabled()) {
				outMsg = "pid_languageCodeISO3" + Constants.LOG_FILE_DELIMITER
						+ "pid.getPID15PrimaryLanguage().getCE1()" + Constants.LOG_FILE_DELIMITER + pid_languageCodeISO3
						+ Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			// Extract marital status
			String pid_MaritalStatusCode = null;
			if (pid.getPID16MaritalStatus() != null) {
				pid_MaritalStatusCode = pid.getPID16MaritalStatus().getCE1();
			}
			party.setMaritalStatusCode(pid_MaritalStatusCode);
			if (logger.isDebugEnabled()) {
				outMsg = "pid_MaritalStatusCode" + Constants.LOG_FILE_DELIMITER + "pid.getPID16MaritalStatus().getCE1()"
						+ Constants.LOG_FILE_DELIMITER + pid_MaritalStatusCode + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			// Extract race code
			String pid_raceCode = null;
			if (pid.getPID10Race() != null && pid.getPID10Race().size() > 0) {
				pid_raceCode = pid.getPID10Race().get(0).getCE1();
			}
			party.setRaceCode(pid_raceCode);
			if (logger.isDebugEnabled()) {
				outMsg = "pid_raceCode" + Constants.LOG_FILE_DELIMITER + "pid.getPID10Race().get(0).getCE1()"
						+ Constants.LOG_FILE_DELIMITER + pid_raceCode + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			// Extract gender code
			String pid_genderCode = null;
			if (pid.getPID8AdministrativeSex() != null) {
				pid_genderCode = pid.getPID8AdministrativeSex();
			}
			party.setAdministrativeGenderCode(pid_genderCode);
			if (logger.isDebugEnabled()) {
				outMsg = "pid_genderCode" + Constants.LOG_FILE_DELIMITER + "pid.getPID8AdministrativeSex()"
						+ Constants.LOG_FILE_DELIMITER + pid_genderCode + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			// Extract religion indicator
			String pid_religionCode = null;
			if (pid.getPID17Religion() != null) {
				pid_religionCode = pid.getPID17Religion().getCE1();
			}
			party.setReligionCode(pid_religionCode);
			if (logger.isDebugEnabled()) {
				outMsg = "pid_religionCode" + Constants.LOG_FILE_DELIMITER + "pid.getPID17Religion().getCE1()"
						+ Constants.LOG_FILE_DELIMITER + pid_religionCode + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			// Extract event's user id
			String evn_UpdateUserId = null;
			if (evn.getEVN5OperatorID() != null && evn.getEVN5OperatorID().size() > 0) {
				evn_UpdateUserId = evn.getEVN5OperatorID().get(0).getXCN1();
			}
			party.setUpdateUserId(evn_UpdateUserId);
			if (logger.isDebugEnabled()) {
				outMsg = "evn_UpdateUserId" + Constants.LOG_FILE_DELIMITER + "evn.getEVN5OperatorID().get(0).getXCN1()"
						+ Constants.LOG_FILE_DELIMITER + evn_UpdateUserId + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			// Extract event's date time
			String evn_UpdateDateTime = null;
			if (evn.getEVN2RecordedDateTime() != null
					&& isNullOrEmpty(evn.getEVN2RecordedDateTime().getTS1()) == false) {
				evn_UpdateDateTime = evn.getEVN2RecordedDateTime().getTS1();
				evn_UpdateDateTime = formatHL7DateTimeToDateTime(evn_UpdateDateTime);
			}
			party.setUpdateDateTime(evn_UpdateDateTime);
			if (logger.isDebugEnabled()) {
				outMsg = "evn_UpdateDateTime" + Constants.LOG_FILE_DELIMITER
						+ "evn.getEVN2RecordedDateTime().getTS1(): " + evn_UpdateDateTime
						+ Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
				outMsg = "-------- END MdmPerson.parseParty --------";
				logger.info(outMsg);
			}

		} catch (Exception e) {
			String msg = "Exception caught in MdmInterfaceMessage.parseMessage.  Exception: " + e;
			logger.error(msg);
			throw new MessageProcessingException(msg);
		}

		return party;
	}

	/**
	 * Parse the HL7 and create List of PartyRole objects Add MC number to the
	 * party role
	 * 
	 * @return List of PartyRole objects
	 * 
	 */
	public static List<PartyRole> parsePartyRole(HL7Segments hl7Segments, Party party) {

		String outMsg = "";

		List<PartyRole> partyRoleList = new ArrayList<PartyRole>();
		PartyRole partyRole = new PartyRole();
		List<RoleAlternateId> roleAlternateIdList = new ArrayList<RoleAlternateId>();
		PIDCONTENT pid = hl7Segments.getPid();
		EVNCONTENT evn = hl7Segments.getEvn();

		if (logger.isDebugEnabled()) {
			outMsg = "-------- BEGIN MdmPerson.parsePartyRole --------";
			logger.debug(outMsg);
		}

		// Extract alternate id and alternate id type
		if (pid.getPID4AlternatePatientIDPID() != null && pid.getPID4AlternatePatientIDPID().size() > 0) {
			for (int i = 0; i < pid.getPID4AlternatePatientIDPID().size(); i++) {
				String altIdNumber = pid.getPID4AlternatePatientIDPID().get(i).getCX1();
				String altIdTypeCode = pid.getPID4AlternatePatientIDPID().get(i).getCX5();
				if (!(isNullOrEmpty(altIdNumber) || isDoubleQuotes(altIdNumber))) {
					RoleAlternateId roleAlternateId = new RoleAlternateId();
					AlternateIdValue alternateIdValue = new AlternateIdValue();
					alternateIdValue.setAlternateIdValue(altIdNumber);
					alternateIdValue.setIdTypeCode(altIdTypeCode);
					roleAlternateId.setAlternateIdValue(alternateIdValue);
					roleAlternateIdList.add(roleAlternateId);
					if (logger.isDebugEnabled()) {
						outMsg = "altIdNumber" + Constants.LOG_FILE_DELIMITER
								+ "pid.getPID4AlternatePatientIDPID().get(i).getCX1()" + Constants.LOG_FILE_DELIMITER
								+ altIdNumber + Constants.LOG_FILE_DELIMITER;
						logger.debug(outMsg);

						outMsg = "altIdTypeCode" + Constants.LOG_FILE_DELIMITER
								+ "pid.getPID4AlternatePatientIDPID().get(i).getCX5()" + Constants.LOG_FILE_DELIMITER
								+ altIdTypeCode + Constants.LOG_FILE_DELIMITER;
						logger.debug(outMsg);
					}
				}
			}
		}
		// Add MC number to the Party Role list
		if (isNullOrEmpty(party.getSourceSystemId()) == false) {
			RoleAlternateId roleAlternateId = new RoleAlternateId();
			AlternateIdValue alternateIdValue = new AlternateIdValue();
			alternateIdValue.setAlternateIdValue(party.getSourceSystemId());
			alternateIdValue.setIdTypeCode(mcTypeCode);
			roleAlternateId.setAlternateIdValue(alternateIdValue);
			roleAlternateIdList.add(roleAlternateId);
			if (logger.isDebugEnabled()) {
				outMsg = "MC number-altIdNumber" + Constants.LOG_FILE_DELIMITER
						+ "pid.getPID4AlternatePatientIDPID().get(i).getCX1()" + Constants.LOG_FILE_DELIMITER
						+ party.getSourceSystemId() + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);

				outMsg = "MC number-altIdTypeCode" + Constants.LOG_FILE_DELIMITER
						+ "pid.getPID4AlternatePatientIDPID().get(i).getCX5()" + Constants.LOG_FILE_DELIMITER
						+ mcTypeCode + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}
		}

		// add MPII number to the Party Role list. This came with the message,
		// but not part of the alternate IDs.
		if (isNullOrEmpty(party.getSourceSystemId()) == false) {
			String mpiiNumber = "";
			if (pid.getPID2PatientID().getCX1() != null) {
				mpiiNumber = pid.getPID2PatientID().getCX1();
				if (logger.isDebugEnabled()) {
					outMsg = "mpiiNumber" + Constants.LOG_FILE_DELIMITER + "pid.getPID2PatientID().getCX1()"
							+ Constants.LOG_FILE_DELIMITER + mpiiNumber + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
			}

			RoleAlternateId roleAlternateId = new RoleAlternateId();
			AlternateIdValue alternateIdValue = new AlternateIdValue();
			alternateIdValue.setAlternateIdValue(mpiiNumber);
			alternateIdValue.setIdTypeCode(sourceSystemName);
			roleAlternateId.setAlternateIdValue(alternateIdValue);
			roleAlternateIdList.add(roleAlternateId);
			if (logger.isDebugEnabled()) {
				outMsg = "MPII number-altIdNumber" + Constants.LOG_FILE_DELIMITER + "pid.getPID2PatientID().getCX1()"
						+ Constants.LOG_FILE_DELIMITER + mpiiNumber + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);

				outMsg = "MPII number-altIdTypeCode" + Constants.LOG_FILE_DELIMITER + "sourceSystemName"
						+ Constants.LOG_FILE_DELIMITER + sourceSystemName + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}
		}
		// add SSN to the Party Role list. This came with the message, but not
		// part of the alternate IDs.
		if (isNullOrEmpty(pid.getPID19SSNNumberPatient()) == false) {
			String ssnNumber = "";
			ssnNumber = pid.getPID19SSNNumberPatient();
			if (logger.isDebugEnabled()) {
				outMsg = "ssnNumber" + Constants.LOG_FILE_DELIMITER + "pid.getPID19SSNNumberPatient()"
						+ Constants.LOG_FILE_DELIMITER + ssnNumber + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}

			RoleAlternateId roleAlternateId = new RoleAlternateId();
			AlternateIdValue alternateIdValue = new AlternateIdValue();
			if (!(isNullOrEmpty(ssnNumber) || isDoubleQuotes(ssnNumber) || ssnNumber.equalsIgnoreCase("000000000"))) {
				alternateIdValue.setAlternateIdValue(ssnNumber);
				alternateIdValue.setIdTypeCode(ssnTypeCode);
				roleAlternateId.setAlternateIdValue(alternateIdValue);
				roleAlternateId.setEffectiveStartDateTime(DoubleQuoteString);
				roleAlternateId.setEffectiveEndDateTime(DoubleQuoteString);
			} else {
				alternateIdValue.setAlternateIdValue("000000000");
				alternateIdValue.setIdTypeCode(ssnTypeCode);
				roleAlternateId.setAlternateIdValue(alternateIdValue);
				roleAlternateId.setEffectiveStartDateTime(DoubleQuoteString);
				roleAlternateId.setEffectiveEndDateTime(party.getUpdateDateTime());
			}
			alternateIdValue.setIdTypeCode(ssnTypeCode);
			roleAlternateId.setAlternateIdValue(alternateIdValue);
			roleAlternateIdList.add(roleAlternateId);
			if (logger.isDebugEnabled()) {
				outMsg = "SSN number-altIdNumber" + Constants.LOG_FILE_DELIMITER + "pid.getPID19SSNNumberPatient()"
						+ Constants.LOG_FILE_DELIMITER + ssnNumber + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);

				outMsg = "SSN number-altIdTypeCode" + Constants.LOG_FILE_DELIMITER + "ssnTypeCode"
						+ Constants.LOG_FILE_DELIMITER + ssnTypeCode + Constants.LOG_FILE_DELIMITER;
				logger.debug(outMsg);
			}
		}

		partyRole.setRoleAlternateId(roleAlternateIdList);

		// Add patient's address
		if (pid.getPID11PatientAddress() != null && pid.getPID11PatientAddress().size() > 0) {
			List<RoleAddr> addressList = new ArrayList<RoleAddr>();
			for (int i = 0; i < pid.getPID11PatientAddress().size(); i++) {
				RoleAddr address = new RoleAddr();

				String addressLine1 = null;
				try {
					addressLine1 = pid.getPID11PatientAddress().get(i).getXAD1().getSAD1();
				} catch (Exception e) {
				}
				address.setAddressLine1(addressLine1);
				if (logger.isDebugEnabled()) {
					outMsg = "addressLine1" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID11PatientAddress().get(i).getXAD1().getSAD1()" + Constants.LOG_FILE_DELIMITER
							+ addressLine1 + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				String addressLine2 = null;
				try {
					addressLine2 = pid.getPID11PatientAddress().get(i).getXAD2();
				} catch (Exception e) {
				}
				address.setAddressLine2(addressLine2);
				if (logger.isDebugEnabled()) {
					outMsg = "addressLine2" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID11PatientAddress().get(i).getXAD2()" + Constants.LOG_FILE_DELIMITER
							+ addressLine2 + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				String addressLine3 = null;
				try {
					addressLine3 = pid.getPID11PatientAddress().get(i).getXAD8();
				} catch (Exception e) {
				}
				address.setAddressLine3(addressLine3);
				if (logger.isDebugEnabled()) {
					outMsg = "addressLine3" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID11PatientAddress().get(i).getXAD8()" + Constants.LOG_FILE_DELIMITER
							+ addressLine3 + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				String cityName = null;
				try {
					cityName = pid.getPID11PatientAddress().get(i).getXAD3();
				} catch (Exception e) {
				}
				address.setCity(cityName);
				if (logger.isDebugEnabled()) {
					outMsg = "cityName" + Constants.LOG_FILE_DELIMITER + "pid.getPID11PatientAddress().get(i).getXAD3()"
							+ Constants.LOG_FILE_DELIMITER + cityName + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				String stateCode = null;
				try {
					stateCode = pid.getPID11PatientAddress().get(i).getXAD4();
				} catch (Exception e) {
				}
				address.setStateCode(stateCode);
				if (logger.isDebugEnabled()) {
					outMsg = "stateCode" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID11PatientAddress().get(i).getXAD4()" + Constants.LOG_FILE_DELIMITER + stateCode
							+ Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				String postalCode = null;
				try {
					postalCode = pid.getPID11PatientAddress().get(i).getXAD5();
				} catch (Exception e) {
				}
				address.setPostalCode(postalCode);
				if (logger.isDebugEnabled()) {
					outMsg = "postalCode" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID11PatientAddress().get(i).getXAD5()" + Constants.LOG_FILE_DELIMITER
							+ postalCode + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				String countryCodeISO3 = null;
				try {
					countryCodeISO3 = pid.getPID11PatientAddress().get(i).getXAD6();
				} catch (Exception e) {
				}
				address.setCountryCodeISO3(countryCodeISO3);
				if (logger.isDebugEnabled()) {
					outMsg = "countryCodeISO3" + Constants.LOG_FILE_DELIMITER
							+ "pid.getPID11PatientAddress().get(i).getXAD6()" + Constants.LOG_FILE_DELIMITER
							+ countryCodeISO3 + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				String county = null;
				try {
					county = pid.getPID12CountyCode();
				} catch (Exception e) {
				}
				address.setCounty(county);
				if (logger.isDebugEnabled()) {
					outMsg = "county" + Constants.LOG_FILE_DELIMITER + "pid.getPID12CountyCode()"
							+ Constants.LOG_FILE_DELIMITER + county + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				// Extract address's update date time (We use the Event's date
				// time)
				String address_UpdateDateTime = null;
				if (evn.getEVN2RecordedDateTime() != null
						&& isNullOrEmpty(evn.getEVN2RecordedDateTime().getTS1()) == false) {
					address_UpdateDateTime = evn.getEVN2RecordedDateTime().getTS1();
					address_UpdateDateTime = formatHL7DateTimeToDateTime(address_UpdateDateTime);
				}
				address.setUpdateDateTime(address_UpdateDateTime);
				if (logger.isDebugEnabled()) {
					outMsg = "address_UpdateDateTime" + Constants.LOG_FILE_DELIMITER
							+ "evn.getEVN2RecordedDateTime().getTS1(): " + address_UpdateDateTime
							+ Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				// Extract address's update user id (We use the event's user id)
				String address_UpdateUserId = null;
				if (evn.getEVN5OperatorID() != null && evn.getEVN5OperatorID().size() > 0) {
					address_UpdateUserId = evn.getEVN5OperatorID().get(0).getXCN1();
				}
				address.setUpdateUserId(address_UpdateUserId);
				if (logger.isDebugEnabled()) {
					outMsg = "address_UpdateUserId" + Constants.LOG_FILE_DELIMITER
							+ "evn.getEVN5OperatorID().get(0).getXCN1()" + Constants.LOG_FILE_DELIMITER
							+ address_UpdateUserId + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}

				address.setAddressTypeCode(partyAddressTypeHome);
				addressList.add(address);
			}
			partyRole.setRoleAddr(addressList);
		}

		// Add patient's attributes

		// WARNING: These have hard coded values which match the lookup values
		// in MDM table C_LK_ATRB_CD.

		ZSFCONTENT zsf = hl7Segments.getZsf();

		if (zsf != null) {

			boolean addAttribute = false;

			// Add Wheel Chair Needed

			List<RoleAttr> attrList = new ArrayList<RoleAttr>();

			RoleAttr wheelChairNeeded = new RoleAttr();

			String wheelChairNeededValue = "";
			try {
				wheelChairNeededValue = zsf.getZSF4WheelChair();
			} catch (Exception e) {
				wheelChairNeededValue = "";
			}
			if (wheelChairNeededValue != null
					&& (wheelChairNeededValue != "" && wheelChairNeededValue.trim().length() > 0)) {
				wheelChairNeeded.setAttributeCode("WHL_CHR_NDD");
				wheelChairNeeded.setAttributeValue(wheelChairNeededValue);

				attrList.add(wheelChairNeeded);
				addAttribute = true;

				if (logger.isDebugEnabled()) {
					outMsg = "wheelChairNeeded" + Constants.LOG_FILE_DELIMITER + "zsf.getZSF4wheelChairNeeded()"
							+ Constants.LOG_FILE_DELIMITER + wheelChairNeededValue + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
			}

			// Add Legally Blind
			RoleAttr legallyBlind = new RoleAttr();

			String legallyBlindValue = "";
			try {
				legallyBlindValue = zsf.getZSF2LegallyBlind();
			} catch (NullPointerException np) {
				legallyBlindValue = "";
			} catch (Exception e) {
				legallyBlindValue = "";
			}
			if (legallyBlindValue != null && (legallyBlindValue != "" && legallyBlindValue.trim().length() > 0)) {
				legallyBlind.setAttributeCode("LGLY_BLND");
				legallyBlind.setAttributeValue(legallyBlindValue);

				attrList.add(legallyBlind);
				addAttribute = true;

				if (logger.isDebugEnabled()) {
					outMsg = "legallyBlind" + Constants.LOG_FILE_DELIMITER + "zsf.getZSF2LegallyBlind()"
							+ Constants.LOG_FILE_DELIMITER + legallyBlindValue + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
			}

			// Add HMO Patient
			RoleAttr hmoPatient = new RoleAttr();

			String hmoPatientValue = "";
			try {
				hmoPatientValue = zsf.getZSF6HMOPatient();
			} catch (NullPointerException np) {
				hmoPatientValue = "";
			} catch (Exception e) {
				hmoPatientValue = "";
			}
			if (hmoPatientValue != null && (hmoPatientValue != "" && hmoPatientValue.trim().length() > 0)) {
				hmoPatient.setAttributeCode("HMO_PAT");
				hmoPatient.setAttributeValue(hmoPatientValue);

				attrList.add(hmoPatient);
				addAttribute = true;

				if (logger.isDebugEnabled()) {
					outMsg = "hmoPatient" + Constants.LOG_FILE_DELIMITER + "zsf.getZSF6HMOPatient()"
							+ Constants.LOG_FILE_DELIMITER + hmoPatientValue + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
			}

			// Add Hearing Impaired
			RoleAttr hearingImpaired = new RoleAttr();

			String hearingImpairedValue = "";
			try {
				hearingImpairedValue = zsf.getZSF3HearingImpaired();
			} catch (NullPointerException np) {
				hearingImpairedValue = "";
			} catch (Exception e) {
				hearingImpairedValue = "";
			}
			if (hearingImpairedValue != null
					&& (hearingImpairedValue != "" && hearingImpairedValue.trim().length() > 0)) {
				hearingImpaired.setAttributeCode("HRNG_IMPRD");
				hearingImpaired.setAttributeValue(hearingImpairedValue);

				attrList.add(hearingImpaired);
				addAttribute = true;

				if (logger.isDebugEnabled()) {
					outMsg = "hearingImpaired" + Constants.LOG_FILE_DELIMITER + "zsf.getZSF3HearingImpaired()"
							+ Constants.LOG_FILE_DELIMITER + hearingImpairedValue + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
			}

			// Add Insulin Taking
			RoleAttr insulinTaking = new RoleAttr();

			String insulinTakingValue = "";
			try {
				insulinTakingValue = zsf.getZSF5InsulinTaking();
			} catch (NullPointerException np) {
				insulinTakingValue = "";
			} catch (Exception e) {
				insulinTakingValue = "";
			}
			if (insulinTakingValue != null && (insulinTakingValue != "" && insulinTakingValue.trim().length() > 0)) {
				insulinTaking.setAttributeCode("INSLN_TKNG");
				insulinTaking.setAttributeValue(insulinTakingValue);

				attrList.add(insulinTaking);
				addAttribute = true;

				if (logger.isDebugEnabled()) {
					outMsg = "insulinTaking" + Constants.LOG_FILE_DELIMITER + "zsf.getZSF5InsulinTaking()"
							+ Constants.LOG_FILE_DELIMITER + insulinTakingValue + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
			}

			// Add Workers Compensation
			RoleAttr workersCompensation = new RoleAttr();

			String workersCompensationValue = "";
			try {
				workersCompensationValue = zsf.getZSF7WorkersComp();
			} catch (NullPointerException np) {
				workersCompensationValue = "";
			} catch (Exception e) {
				workersCompensationValue = "";
			}
			if (workersCompensationValue != null
					&& (workersCompensationValue != "" && workersCompensationValue.trim().length() > 0)) {
				workersCompensation.setAttributeCode("WRKRS_CMP");
				workersCompensation.setAttributeValue(workersCompensationValue);

				attrList.add(workersCompensation);
				addAttribute = true;

				if (logger.isDebugEnabled()) {
					outMsg = "workersCompensation" + Constants.LOG_FILE_DELIMITER + "zsf.getZSF7WorkersComp()"
							+ Constants.LOG_FILE_DELIMITER + workersCompensationValue + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
			}

			// Add Interpreter Needed
			RoleAttr interpreterNeeded = new RoleAttr();

			String interpreterNeededValue = "";
			try {
				interpreterNeededValue = zsf.getZSF8Interpreter();
			} catch (NullPointerException np) {
				interpreterNeededValue = "";
			} catch (Exception e) {
				interpreterNeededValue = "";
			}
			if (interpreterNeededValue != null
					&& (interpreterNeededValue != "" && interpreterNeededValue.trim().length() > 0)) {
				interpreterNeeded.setAttributeCode("INTRPRTR_NDD");
				interpreterNeeded.setAttributeValue(interpreterNeededValue);

				attrList.add(interpreterNeeded);
				addAttribute = true;

				if (logger.isDebugEnabled()) {
					outMsg = "interpreterNeeded" + Constants.LOG_FILE_DELIMITER + "zsf.getZSF8Interpreter()"
							+ Constants.LOG_FILE_DELIMITER + interpreterNeededValue + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
			}

			// Add Confidential
			RoleAttr confidential = new RoleAttr();

			PV1CONTENT pv1 = hl7Segments.getPv1();

			String confidentialValue = "";
			try {
				confidentialValue = pv1.getPV116VIPIndicator();
			} catch (NullPointerException np) {
				confidentialValue = "";
			} catch (Exception e) {
				confidentialValue = "";
			}
			if (confidentialValue != null && (confidentialValue != "" && confidentialValue.trim().length() > 0)) {
				confidential.setAttributeCode("CONFLTY_IND");
				confidential.setAttributeValue(confidentialValue);

				attrList.add(confidential);
				addAttribute = true;

				if (logger.isDebugEnabled()) {
					outMsg = "confidential" + Constants.LOG_FILE_DELIMITER + "pv1.getPV116VIPIndicator()"
							+ Constants.LOG_FILE_DELIMITER + confidentialValue + Constants.LOG_FILE_DELIMITER;
					logger.debug(outMsg);
				}
			}

			// Add the list to partyRole
			if (addAttribute == true) {
				partyRole.setRoleAttr(attrList);
			}

		}

		// add phone number
		if (pid.getPID13PhoneNumberHome() != null && pid.getPID13PhoneNumberHome().size() > 0) {
			List<RoleComm> phoneList = new ArrayList<RoleComm>();
			for (int i = 0; i < pid.getPID13PhoneNumberHome().size(); i++) {
				RoleComm phone = MdmInterfaceMessage.parseHL7PhoneFullNumber(
						pid.getPID13PhoneNumberHome().get(i).getXTN1(), pid.getPID13PhoneNumberHome().get(i).getXTN5(),
						partyCommunicationTypePhone, partyCommunicationUsageType);

				// Extract phone's update date time (We use the Event's date
				// time)
				if (evn.getEVN2RecordedDateTime() != null
						&& isNullOrEmpty(evn.getEVN2RecordedDateTime().getTS1()) == false) {
					String phone_UpdateDateTime = evn.getEVN2RecordedDateTime().getTS1();
					phone_UpdateDateTime = formatHL7DateTimeToDateTime(phone_UpdateDateTime);
					phone.setUpdateDateTime(phone_UpdateDateTime);
					if (logger.isDebugEnabled()) {
						outMsg = "address_UpdateDateTime" + Constants.LOG_FILE_DELIMITER
								+ "evn.getEVN2RecordedDateTime().getTS1(): " + phone_UpdateDateTime
								+ Constants.LOG_FILE_DELIMITER;
						logger.debug(outMsg);
					}
				}

				// Extract address's update user id (We use the event's user id)
				if (evn.getEVN5OperatorID() != null && evn.getEVN5OperatorID().size() > 0) {
					String phone_UpdateUserId = evn.getEVN5OperatorID().get(0).getXCN1();
					phone.setUpdateUserId(phone_UpdateUserId);
					if (logger.isDebugEnabled()) {
						outMsg = "address_UpdateUserId" + Constants.LOG_FILE_DELIMITER
								+ "evn.getEVN5OperatorID().get(0).getXCN1()" + Constants.LOG_FILE_DELIMITER
								+ phone_UpdateUserId + Constants.LOG_FILE_DELIMITER;
						logger.debug(outMsg);
					}
				}

				phoneList.add(phone);
			}
			partyRole.setRoleComm(phoneList);
		}

		// Add hard coded party role type code
		partyRole.setRoleTypeCode(partyRoleType);
		partyRole.setClassificationCode(partyRoleType);
		partyRole.setSubClassificationCode(partyRoleType);

		partyRoleList.add(partyRole);

		if (logger.isDebugEnabled()) {
			outMsg = "-------- END MdmPerson.parsePartyRole --------";
			logger.debug(outMsg);
		}

		return partyRoleList;
	}

	/**
	 * Parse the HL7 and create List of PartyNames objects
	 * 
	 * @return List of PartyNames objects
	 * 
	 */
	public static List<PartyNames> parsePartyNames(HL7Segments hl7Segments, Party party) {

		String outMsg = "";

		PIDCONTENT pid = hl7Segments.getPid();

		if (logger.isDebugEnabled()) {
			outMsg = "-------- BEGIN MdmPerson.parsePartyNames --------";
			logger.debug(outMsg);
		}

		List<PartyNames> partyNamesList = new ArrayList<PartyNames>();

		// Extract Maiden Name
		if (pid.getPID6MothersMaidenName().get(0) != null) {

			logger.debug("Parsing maiden name");

			PartyNames maidenName = new PartyNames();

			maidenName.setNameTypeCode("MDN");

			String pid_maidenName = null;
			if (pid.getPID6MothersMaidenName().get(0).getXPN1() != null
					&& pid.getPID6MothersMaidenName().get(0).getXPN1().getFN1() != null
					&& pid.getPID6MothersMaidenName().get(0).getXPN1().getFN1().trim().length() > 0) {
				pid_maidenName = cleanString(pid.getPID6MothersMaidenName().get(0).getXPN1().getFN1());
			}
			maidenName.setLastName(pid_maidenName);

			// Extract First Name
			String pid_firstName = null;
			if (pid.getPID6MothersMaidenName().get(0).getXPN2() != null
					&& pid.getPID6MothersMaidenName().get(0).getXPN2().trim().length() > 0) {
				pid_firstName = pid.getPID6MothersMaidenName().get(0).getXPN2().trim();
			} else {
				// Default to using the party level first name if a value not
				// provided in PID6
				if (pid.getPID5PatientName().get(0).getXPN2() != null
						&& pid.getPID5PatientName().get(0).getXPN2().trim().length() > 0)
					pid_firstName = pid.getPID5PatientName().get(0).getXPN2().trim();
			}

			maidenName.setFirstName(pid_firstName);

			// Extract Middle Name
			String pid_middleName = null;
			if (pid.getPID6MothersMaidenName().get(0).getXPN3() != null
					&& pid.getPID6MothersMaidenName().get(0).getXPN3().trim().length() > 0) {
				pid_middleName = pid.getPID6MothersMaidenName().get(0).getXPN3().trim();
			} else {
				// Default to using the party level middle name if a value not
				// provided in PID6
				if (pid.getPID5PatientName().get(0).getXPN3() != null
						&& pid.getPID5PatientName().get(0).getXPN3().trim().length() > 0)
					pid_middleName = pid.getPID5PatientName().get(0).getXPN3().trim();
			}
			maidenName.setMiddleName(pid_middleName);

			// Extract Prefix
			String pid_prefixName = null;
			if (pid.getPID6MothersMaidenName().get(0).getXPN5() != null
					&& pid.getPID6MothersMaidenName().get(0).getXPN5().trim().length() > 0) {
				pid_prefixName = pid.getPID6MothersMaidenName().get(0).getXPN5().trim();
			} else {
				// Default to using the party level prefix if a value not
				// provided in PID6
				if (pid.getPID5PatientName().get(0).getXPN5() != null
						&& pid.getPID5PatientName().get(0).getXPN5().trim().length() > 0)
					pid_prefixName = pid.getPID5PatientName().get(0).getXPN5().trim();
			}
			maidenName.setPrefixNameCode(pid_prefixName);

			// Extract Suffix
			String pid_suffixName = null;
			if (pid.getPID6MothersMaidenName().get(0).getXPN4() != null
					&& pid.getPID6MothersMaidenName().get(0).getXPN4().trim().length() > 0) {
				pid_suffixName = pid.getPID6MothersMaidenName().get(0).getXPN4().trim();
			} else {
				// Default to using the party level suffix
				if (pid.getPID5PatientName().get(0).getXPN4() != null
						&& pid.getPID5PatientName().get(0).getXPN4().trim().length() > 0)
					pid_suffixName = pid.getPID5PatientName().get(0).getXPN4().trim();
			}
			maidenName.setSuffixNameCode(pid_suffixName);

			partyNamesList.add(maidenName);
		}

		// Parse Patient name
		if (pid.getPID5PatientName().get(0) != null && pid.getPID5PatientName().get(0).getXPN1() != null) {

			logger.debug("Parsing patient name");

			PartyNames patientName = new PartyNames();

			patientName.setNameTypeCode("PTN");

			String pid_lastName = null;
			if (pid.getPID5PatientName().get(0).getXPN1().getFN1() != null
					&& pid.getPID5PatientName().get(0).getXPN1().getFN1().trim().length() > 0) {
				pid_lastName = pid.getPID5PatientName().get(0).getXPN1().getFN1();
			}
			patientName.setLastName(pid_lastName);

			// Extract First Name
			String pid_firstName = null;
			if (pid.getPID5PatientName().get(0).getXPN2() != null
					&& pid.getPID5PatientName().get(0).getXPN2().trim().length() > 0) {
				pid_firstName = pid.getPID5PatientName().get(0).getXPN2();
			}
			patientName.setFirstName(pid_firstName);

			// Extract Middle Name
			String pid_middleName = null;
			if (pid.getPID5PatientName().get(0).getXPN3() != null
					&& pid.getPID5PatientName().get(0).getXPN3().trim().length() > 0) {
				pid_middleName = pid.getPID5PatientName().get(0).getXPN3();
			}
			patientName.setMiddleName(pid_middleName);

			// Extract Prefix
			String pid_prefixName = null;
			if (pid.getPID5PatientName().get(0).getXPN5() != null
					&& pid.getPID5PatientName().get(0).getXPN5().trim().length() > 0) {
				pid_prefixName = pid.getPID5PatientName().get(0).getXPN5();
			}
			patientName.setPrefixNameCode(pid_prefixName);

			// Extract Suffix
			String pid_suffixName = null;
			if (pid.getPID5PatientName().get(0).getXPN4() != null
					&& pid.getPID5PatientName().get(0).getXPN4().trim().length() > 0) {
				pid_suffixName = pid.getPID5PatientName().get(0).getXPN4();
			}
			patientName.setSuffixNameCode(pid_suffixName);

			partyNamesList.add(patientName);
		}

		return partyNamesList;
	}

	public static Party parsePriorParty(HL7Segments hl7Segments) {
		Party party = new Party();
		MRGCONTENT mrg = hl7Segments.getMrg();
		String mcNumber = "";
		// Extract MC code

		try {
			List<CX> ppIdList = mrg.getMRG1PriorPatientIdentifierList();
			if (ppIdList != null && !ppIdList.isEmpty()) {
				for (CX priorPatientId : ppIdList) {
					String mcTypeCodeIncoming = priorPatientId.getCX5();
					if (mcTypeCode.equals(mcTypeCodeIncoming)) {
						mcNumber = priorPatientId.getCX1();
						if (mcNumber == null || mcNumber.trim().length() == 0) {
							String msg = "Null/empty value for MC code value in message element MRG.1.PriorPatientIdentifierList.CX.1";
							logger.error(msg);
							throw new MessageProcessingException(msg);
						}
						String sourceSystemNumberPrefix = appConfig.getSourceSystemPrefix();
						if (sourceSystemNumberPrefix != null && sourceSystemNumberPrefix.trim().length() > 0) {
							mcNumber = sourceSystemNumberPrefix + mcNumber.trim();
							logger.debug(">>>>> Event code: " + hl7Segments.getEvn().getEVN1EventTypeCode()
									+ " -- Setting static prefix: " + sourceSystemNumberPrefix + " for MC number: "
									+ mcNumber);
						}
						break;
					}
				}
			}
			// Set MC code and source system name
			// Source system name is hard-coded to MPII value
			if (!(isNullOrEmpty(mcNumber) || isDoubleQuotes(mcNumber))) {
				logger.debug("Found prior party by MC number.  Source System Name:" + sourceSystemName);
				party.setSourceSystemName(sourceSystemName.trim());
				party.setSourceSystemId(mcNumber.trim());
			} else { // Try to look up by MPII ID if provided.
				CX ppId = mrg.getMRG4PriorPatientID();
				String mpiiId = null;
				if (ppId != null) {
					mpiiId = ppId.getCX1();
					logger.debug("In parsePriorParty.  MPII ID found.  MPII ID:" + mpiiId);
				}

				if (!(isNullOrEmpty(mpiiId))) {

					logger.debug("In parsePriorParty.  MPII ID is null.  Looking up by alt id.");
					// Look up Party By MPII Alt ID.
					MdmPersonDao mdmPersonDao = new MdmPersonDao();
					SearchPartyRequest searchPartyRequest = new SearchPartyRequest();

					// add credentials to the request object.
					UserCredentials userCredentials = new UserCredentials();
					String username = configProperties.getProperty("auth_usr");
					String pwd = configProperties.getProperty("auth_pwd");
					userCredentials.setUserName(username);
					userCredentials.setPassword(pwd);

					AlternateIdValue alternateIdValue = new AlternateIdValue();
					alternateIdValue.setAlternateIdValue(mpiiId);
					alternateIdValue.setIdTypeCode("MPII");
					searchPartyRequest.getAlternateIdValue().add(alternateIdValue);
					searchPartyRequest.setUserCredentials(userCredentials);
					MdmInterfaceMessage mdmPerson = new MdmInterfaceMessage(searchPartyRequest);

				}
			}

		} catch (Exception e) {
			String msg = "Error in parsePriorParty.  Error:" + e;
			logger.error(msg);
			throw new MessageProcessingException(msg);
		}

		return party;
	}

	public static String appendZeroTimeToDate(String fromDate) {
		String toDateString = "";
		DateFormat fromFormat = new SimpleDateFormat("yyyyMMdd");
		fromFormat.setLenient(true);
		DateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00"); // 1940-09-12
																			// 00:00:00
		if (isNullOrEmpty(fromDate) || isDoubleQuotes(fromDate)) {
			toDateString = "";
		} else {
			try {
				toDateString = toFormat.format(fromFormat.parse(fromDate));
			} catch (ParseException e) {
			}
		}
		return toDateString;
	}

	public static String formatHL7DateTimeToDateTime(String _fromDate) {

		String fromDate = "";
		String toDateString = "";
		Boolean goodDate = true;

		if (isNullOrEmpty(_fromDate)) {
			toDateString = "";
		} else {
			try {
				fromDate = filterDateTime(_fromDate);
				DateFormat fromFormat = null;

				if (logger.isDebugEnabled()) {
					logger.debug("fromDate:" + _fromDate + " -- string length:" + fromDate.length());
				}

				/*
				 * David Mecca - HighPoint Solutions - 01092018 - adding date
				 * padding for bug #1233326
				 * 
				 */
				if (fromDate.length() == 13 || fromDate.length() == 11 || fromDate.length() == 9
						|| fromDate.length() == 7) {
					if (logger.isWarnEnabled()) {
						logger.warn("Message date not a standard length: " + fromDate.length()
								+ " -- Right padding with zeroes.  Message date value:" + fromDate);
					}
					fromDate = Strings.padEnd(fromDate, 14, '0');
					if (logger.isDebugEnabled())
						logger.debug("Corrected date value: " + fromDate);
				}

				if (fromDate.length() >= 14) {
					fromFormat = new SimpleDateFormat("yyyyMMddHHmmss");
				} else if (fromDate.length() == 12) {
					fromFormat = new SimpleDateFormat("yyyyMMddHHmm");
				} else if (fromDate.length() == 10) {
					fromFormat = new SimpleDateFormat("yyyyMMddHH");
				} else if (fromDate.length() == 8) {
					fromFormat = new SimpleDateFormat("yyyyMMdd");
				} else if (isDoubleQuotes(fromDate)) {
					logger.debug("Date string has double quotes.");
				} else {
					goodDate = false;
				}

				if (goodDate) {
					if (isDoubleQuotes(fromDate)) {
						// Use of Double Quotes is intended to remove/set null a
						// data item.
						toDateString = "\"\"";
					} else {
						fromFormat.setLenient(true);
						DateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						toDateString = toFormat.format(fromFormat.parse(fromDate));
						if (logger.isDebugEnabled()) {
							logger.debug("Formatted date string:" + toDateString);
						}
					}
				} else {
					logger.warn("formatHL7DateTimeToDateTime(): BAD fromDate = " + fromDate);
				}

			} catch (ParseException e) {
			}
		}
		return toDateString;
	}

	public static RoleComm parseHL7PhoneFullNumber(String phoneFull, String countryCode, String communicationType,
			String communicationUsageType) {

		String outMsg = "";
		String phoneCountryCode = "1";

		if (logger.isDebugEnabled()) {
			outMsg = "-------- BEGIN MdmPerson.parseHL7PhoneFullNumber --------";
			logger.debug(outMsg);
		}

		/*
		 * Here�s an explanation. I hope it�s meaningful:
		 * 
		 * 1. Only valid numeric phone numbers are stored in COMM_VAL. Instead
		 * of (507)284-9544 I will store 5072849544. 2. By (nnn)nnn-nnnn I mean
		 * (507)285-9544. That is the most common way MPII sends the phone
		 * numbers. 3. The value received will always be stored in the CMNT
		 * field. 4. If there is a problem with the phone number,
		 * "INVALID phone number" will be stored in the CMNT field. 5. If the
		 * received phone number has 10 numeric digits they are parsed and
		 * stored in PHN_AREA_CD and PHN_NUM. 6. All non-numeric digits are
		 * stripped from the incoming phone number, and the remaining is tested
		 * if its 10 digits. 7. The Java phone validation checks area codes,
		 * etc. If the number is valid, it's stored in COMM_VAL and the original
		 * stored in CMNT.
		 *
		 * Here�s some examples:
		 * 
		 * Received COMM_VAL PHN_CNTRY_CD PHN_AREA_CD PHN_NUM CMNT (507)284-9544
		 * 5072849544 1 507 2849544 (507)284-9544 (555)555-5555 5555555555 1 555
		 * 5555555 (555)555-5555#INVALID Phone Number (635)879-4125 6358794125 1
		 * 635 8794125 (635)879-4125#INVALID Phone Number A7 -- 1 -- --
		 * A7#INVALID Phone Number 5072849544 5072849544 1 507 2849544
		 * 5072849544 507-284-9544 5072849544 1 507 2849544
		 * 507-284-9544#MODIFIED Phone Number 507-kkl2849544 5072849544 1 507
		 * 2849544 507-kkl2849544#MODIFIED Phone Number x49544 -- 1 -- --
		 * X48544#INVALID Phone Number 49544 -- 1 -- -- 49544#INVALID Phone
		 * Number
		 */

		RoleComm roleComm = new RoleComm();
		if (isNullOrEmpty(countryCode) || isDoubleQuotes(countryCode)) {
			countryCode = "US";
		}

		/*
		 * THIS CODE only files 10 digit phone numbers, regardless of their
		 * validity.
		 */

		if (!isNullOrEmpty(phoneFull)) {
			if (validatePhoneNumber(phoneFull)) {
				if (logger.isDebugEnabled()) {
					outMsg = "phoneFull" + Constants.LOG_FILE_DELIMITER + phoneFull;
					logger.debug(outMsg);
				}

				String numericPhoneNum = phoneFull;

				numericPhoneNum = numericPhoneNum.replaceAll("[^0-9]", "");
				roleComm.setCommunicationTypeCode(communicationType);
				roleComm.setUsageTypeCode(communicationUsageType);
				roleComm.setPhoneCountryCode(phoneCountryCode);
				roleComm.setPhoneAreaCode(numericPhoneNum.substring(0, 3));
				roleComm.setPhoneNumber(numericPhoneNum.substring(3, 10));
				if (validatePhoneNumber(phoneFull) == false) {
					roleComm.setCommunicationValue(numericPhoneNum);
					roleComm.setComment(phoneFull + Constants.LOG_FILE_DELIMITER + "INVALID Phone Number");
					if (logger.isDebugEnabled()) {
						outMsg = "INVALID phone" + Constants.LOG_FILE_DELIMITER + phoneFull;
						logger.debug(outMsg);
					}
				} else {
					roleComm.setCommunicationValue(phoneFull.replaceAll("[^\\d.]", ""));
					roleComm.setComment(phoneFull);
					if (logger.isDebugEnabled()) {
						outMsg = "VALID phone" + Constants.LOG_FILE_DELIMITER + phoneFull;
						logger.debug(outMsg);
					}
				}
			} else {
				logger.error("Invalid phone number provided.  Data will not ignored.  Value:"
						+ Constants.LOG_FILE_DELIMITER + phoneFull);
			}
		} else {
			if (logger.isDebugEnabled()) {
				outMsg = "Phone number element is empty/null";
				logger.debug(outMsg);
			}
		}

		if (logger.isDebugEnabled())

		{
			outMsg = "-------- END MdmPerson.parseHL7PhoneFullNumber --------";
			logger.debug(outMsg);
		}

		return roleComm;
	}

	private static boolean validatePhoneNumber(String phoneNo) {
		/*
		 * Phone number 1234567890 validation result: true Phone number
		 * 123-456-7890 validation result: true Phone number 123-456-7890 x1234
		 * validation result: true Phone number 123-456-7890 ext1234 validation
		 * result: true Phone number (123)456-7890 validation result: true Phone
		 * number 123.456.7890 validation result: true Phone number 123 456 7890
		 * validation result: true
		 */

		// validate phone numbers of format "1234567890"
		if (phoneNo.matches("\\d{10}"))
			return true;
		// validating phone number with -, . or spaces
		else if (phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}"))
			return true;
		// validating phone number where area code is in braces ()
		else if (phoneNo.matches("\\(\\d{3}\\)\\d{3}-\\d{4}"))
			return true;
		return false;
	}

	public static boolean isNullOrEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public static boolean isDoubleQuotes(String s) {
		boolean containsADoubleQuote = false;
		if (("\"\"").equalsIgnoreCase(s)) {
			containsADoubleQuote = true;
		}
		return containsADoubleQuote;
	}

	public static String filterDateTime(String dt) {
		String filteredDT = null;
		String rexexPattern = "-\\d\\d\\d\\d$";
		if (dt != null && dt.length() > 0) {
			filteredDT = dt.replaceAll(rexexPattern, "");
		}
		return filteredDT;
	}

	public String getOverallProcessingResponseStatus() {

		StringBuffer sb = new StringBuffer();
		sb.append("Overall inbound status per base object:").append(nl);
		if (putPartyResponse.getPartyResponseStatus() != null) {
			sb.append(">>partyResponseStatus:").append(nl);
			sb.append(putPartyResponse.getPartyResponseStatus().toString(4));
		} else {
			sb.append(">>partyResponseStatus is NULL").append(nl);
		}
		if (putPartyResponse.getPartyRoleResponseStatus() != null) {
			sb.append(">>partyRoleResponseStatus:").append(nl);
			sb.append(putPartyResponse.getPartyRoleResponseStatus().toString(4));
		} else {
			sb.append(">>partyRoleResponseStatus is NULL").append(nl);
		}
		if (putPartyResponse.getAlternateIdResponseStatus() != null) {
			sb.append(">>alternateIdResponseStatus:").append(nl);
			sb.append(putPartyResponse.getAlternateIdResponseStatus().toString(4));
		} else {
			sb.append(">>alternateIdResponseStatus is NULL").append(nl);
		}
		if (putPartyResponse.getNameResponseStatus() != null) {
			sb.append(">>nameResponseStatus:").append(nl);
			sb.append(putPartyResponse.getNameResponseStatus().toString(4));
		} else {
			sb.append(">>nameResponseStatus is NULL").append(nl);
		}
		if (putPartyResponse.getAttributeResponseStatus() != null) {
			sb.append(">>attributeResponseStatus:").append(nl);
			sb.append(putPartyResponse.getAttributeResponseStatus().toString(4));
		} else {
			sb.append(">>attributeResponseStatus is NULL").append(nl);
		}
		if (putPartyResponse.getAddressResponseStatus() != null) {
			sb.append(">>addressResponseStatus:").append(nl);
			sb.append(putPartyResponse.getAddressResponseStatus().toString(4));
		} else {
			sb.append(">>addressResponseStatus is NULL").append(nl);
		}
		if (putPartyResponse.getCommunicationResponseStatus() != null) {
			sb.append(">>communicationResponseStatus:").append(nl);
			sb.append(putPartyResponse.getCommunicationResponseStatus().toString(4)).append(nl);
		} else {
			sb.append(">>communicationResponseStatus is NULL").append(nl);
		}

		return sb.toString();

	}

	private static String cleanString(String value) {

		return value.trim().replaceAll("(^')|('$)", "") // Remove begin/end
														// single
														// quotes
				.replaceAll("(^\")|(\"$)", ""); // Remove begin/end
												// double quotes

	}

}
