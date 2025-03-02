package ru.elessarov.survey_bot.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;
import ru.elessarov.survey_bot.model.Survey;
import ru.elessarov.survey_bot.model.UserState;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class GoogleApiService {
	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
	private static final String TOKENS_DIRECTORY_PATH = "tokens/path";
	private static final String RAW = "RAW";
	private static final String DEFAULT_RANGE = "!A1";
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
	private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final Pattern EXTRACT_GID_PATTERN = Pattern.compile("gid=([0-9]+)");
	private static final Pattern EXTRACT_SPREADSHEET_ID_PATTERN = Pattern.compile("/d/([a-zA-Z0-9-_]+)");


	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		InputStream in = GoogleApiService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT,
				JSON_FACTORY,
				clientSecrets,
				SCOPES
		).setDataStoreFactory(new FileDataStoreFactory(new File(System.getProperty("user.home"), TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	private Sheets getSheetService() throws GeneralSecurityException, IOException {
		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		return new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
				.setApplicationName(APPLICATION_NAME).build();
	}

	public void writeAnswersToSheet(UserState userState, Survey survey, boolean initialize) {
        try {
            Sheets service = getSheetService();
        	List<Object> rawData;
			if (initialize) {
				rawData = survey.getNamesForInitializing();
				survey.setInitialized(true);
			} else {
				rawData = userState.getAnswersRawData();
			}
			List<List<Object>> values = List.of(rawData);
			ValueRange valueRange = new ValueRange().setValues(values);

			String spreadsheetID = extractSpreadsheetID(survey.getLink());
			String sheetName = extractSpreadsheetName(service, spreadsheetID, survey.getLink());
			String range = sheetName + DEFAULT_RANGE;

			service.spreadsheets()
			   .values()
			   .append(extractSpreadsheetID(survey.getLink()), range, valueRange)
			   .setValueInputOption(RAW)
			   .execute();
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String extractSpreadsheetID(String url) {
		Matcher matcher = EXTRACT_SPREADSHEET_ID_PATTERN.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private static String extractGid(String url) {
		Matcher matcher = EXTRACT_GID_PATTERN.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "0";
	}

	private static String extractSpreadsheetName(Sheets service, String spreadsheetId, String url) {
		String gid = extractGid(url);
		Spreadsheet spreadsheet;
		try {
			spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (Sheet sheet : spreadsheet.getSheets()) {
			if (sheet.getProperties().getSheetId().toString().equals(gid)) {
				return sheet.getProperties().getTitle();
			}
		}
		throw new RuntimeException("Вкладка с указанным Gid - %s, не найдена".formatted(gid));
	}
}
