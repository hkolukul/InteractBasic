package com.unicacorp.interact.samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.json.JSONException;

import com.unicacorp.interact.api.AdvisoryMessage;
import com.unicacorp.interact.api.BatchResponse;
import com.unicacorp.interact.api.Command;
import com.unicacorp.interact.api.CommandImpl;
import com.unicacorp.interact.api.NameValuePair;
import com.unicacorp.interact.api.NameValuePairImpl;
import com.unicacorp.interact.api.Offer;
import com.unicacorp.interact.api.OfferList;
import com.unicacorp.interact.api.Response;
import com.unicacorp.interact.api.rest.RestClientConnector;
import com.unicacorp.interact.testclient.JavaPrintUtil;

public class InteractRestClient {

	public static void main(String[] args) throws IOException, JSONException {

		String url = args.length > 0 ? args[0] : "http://dev-ogm-int-rt-s1.optum.com:9080/interact";
		url += "/servlet/RestServlet";
		String sessionId = args.length > 1 ? args[1] : String.valueOf(System.currentTimeMillis());
		String icName = args.length > 2 ? args[2] : "Hari_Ic";
		String ipName = args.length > 3 ? args[3] : "ip_credit";
		int numberRequested = args.length > 4 ? Integer.valueOf(args[4]) : 5;
		String eventName="contact";
		
		
		List<Command> cmds = new ArrayList<Command>();
		cmds.add(0, createStartSessionCommand(icName));
		cmds.add(1, createGetOffersCommand(ipName, numberRequested));
		cmds.add(2, createGetProfileCommand());
		cmds.add(3, createEndSessionCommand());
		cmds.add(4, createPostEventCommand(eventName));

		RestClientConnector.initialize();
		RestClientConnector connector = new RestClientConnector(url);
		Command[] cmd = { cmds.get(0) };
		BatchResponse start_response = connector.executeBatch(sessionId, cmd, null, null);
		System.out.println("Start session " +start_response.getBatchStatusCode());
		Response[] responses = start_response.getResponses();

		if (start_response.getBatchStatusCode() > 0) {
			for (Response res : responses) {
				AdvisoryMessage[] ams = res.getAdvisoryMessages();
				for (AdvisoryMessage am : ams) {
					System.out.println(am.getMessage());
				}
			}
		}

		Command[] cmd1 = { cmds.get(1),cmds.get(4) };
		BatchResponse response = connector.executeBatch(sessionId, cmd1, null, null);
		System.out.println("Execute batch "+response.getBatchStatusCode());
		responses = response.getResponses();
		for (Response res : responses) {
			OfferList oflist = res.getOfferList();
			if (oflist != null) {
			for (int i = 0; i < oflist.getRecommendedOffers().length; i++) {
				if (oflist.getRecommendedOffers()[i] != null) {
					System.out.println("value is i "+i);
					Offer ofs = oflist.getRecommendedOffers()[i];
					System.out.println(ofs.getOfferName());
					System.out.println(ofs.getTreatmentCode());
					System.out.println("score " + ofs.getScore());
					NameValuePair[] attributes = ofs.getAdditionalAttributes();
					for (NameValuePair attribute : attributes) {
						if (attribute.getValueDataType().equalsIgnoreCase("datetime"))
							System.out.println(attribute.getName() + " " + attribute.getValueAsDate());
						if (attribute.getValueDataType().equalsIgnoreCase("numeric"))
							System.out.println(attribute.getName() + " " + attribute.getValueAsNumeric());
						if (attribute.getValueDataType().equalsIgnoreCase("string"))
							System.out.println(attribute.getName() + " " + attribute.getValueAsString());
					}
					System.out.println("----------------------------------------------");
				}
			}
			}
		}
		
		Command[] cmd_profile = { cmds.get(2)};
		response = connector.executeBatch(sessionId, cmd_profile, null, null);
		System.out.println("Profile "+response.getBatchStatusCode());
		responses = response.getResponses();
		for (Response res:responses) {
			NameValuePair[] npvs = res.getProfileRecord();
			for (NameValuePair attribute : npvs) {
				if (attribute.getValueDataType().equalsIgnoreCase("datetime"))
					System.out.println(attribute.getName() + " " + attribute.getValueAsDate());
				if (attribute.getValueDataType().equalsIgnoreCase("numeric"))
					System.out.println(attribute.getName() + " " + attribute.getValueAsNumeric());
				if (attribute.getValueDataType().equalsIgnoreCase("string"))
					System.out.println(attribute.getName() + " " + attribute.getValueAsString());
			}
			
		}

		System.out.println("----------------------------------------------");
		
		
		Command[] cmd_end = { cmds.get(3) };
		response = connector.executeBatch(sessionId, cmd_end, null, null);
		System.out.println("End "+response.getBatchStatusCode());
	}

	private static Command createStartSessionCommand(String icName) throws JSONException {
		CommandImpl cmd = new CommandImpl();
		cmd.setMethodIdentifier(Command.COMMAND_STARTSESSION);
		cmd.setInteractiveChannel(icName);
		cmd.setAudienceLevel("MDM_Person");
		cmd.setAudienceID(new NameValuePairImpl[] {
				new NameValuePairImpl("MDM_PERSON_ID", NameValuePair.DATA_TYPE_STRING, "1") });
		return cmd;
	}

	private static Command createGetOffersCommand(String ipName, int numberRequested) throws JSONException {
		CommandImpl cmd = new CommandImpl();
		cmd.setMethodIdentifier(Command.COMMAND_GETOFFERS);
		cmd.setInteractionPoint(ipName);
		cmd.setNumberRequested(numberRequested);
		return cmd;
	}
	
	private static Command createPostEventCommand(String eventName) throws JSONException {
		CommandImpl cmd = new CommandImpl();
		cmd.setMethodIdentifier(Command.COMMAND_POSTEVENT);
		cmd.setEvent(eventName);
		return cmd;
	}

	private static Command createGetProfileCommand() throws JSONException {
		CommandImpl cmd = new CommandImpl();
		cmd.setMethodIdentifier(Command.COMMAND_GETPROFILE);
		return cmd;
	}

	private static Command createEndSessionCommand() throws JSONException {
		CommandImpl cmd = new CommandImpl();
		cmd.setMethodIdentifier(Command.COMMAND_ENDSESSION);
		return cmd;
	}
}
