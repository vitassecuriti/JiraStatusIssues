import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by VSKryukov on 02.11.2015.
 */
public class JavaGetIssue {

    public static void main(String[] args) throws URISyntaxException, IOException {

        if (args.length < 6){
            System.out.println("Wrong number parameters");
            System.out.println("MustHave parameters:");
            System.out.println("1st - URL Jira. For exampel: \"https://jira.at-consulting.ru\" " );
            System.out.println("2nd - Login in Jira. For exampel: \"iivanov\" " );
            System.out.println("3rd - Password in Jira. For exampel: \"*********\" " );
            System.out.println("4th - URL Google SpreadSheet's script. For exampel: \"https://script.google.com/macros/s/AKfycbwoymePZ6tJlAWPDQlL0lESe_bcBsQoP5ldgSIsdsGJKyOoASw/exec\" " );
            System.out.println("5th - Filter in Jira. For exampel: \"project=CMRS and fixVersion = \\\"reliz - 30.11.2015 - 2.2.2.0\\\"\" " );
            System.out.println("6th - Sheet's name in Google SpreadSheet. For exampel: \"Tasks 30.11.2015\"" );
            return;
        }

        final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
        final URI jiraServerUri = new URI(args[0]);
        //String GoogleUri = "https://script.google.com/macros/s/AKfycbwoymePZ6tJlAWPDQlL0lESe_bcBsQoP5ldgSIsdsGJKyOoASw/exec"; //Test



        final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, args[1], args[2]);
        final NullProgressMonitor pm = new NullProgressMonitor();
        //final Issue issue = restClient.getIssueClient().getIssue("CMRS-2230", pm);


//        String GoogleUri = "https://script.google.com/macros/s/AKfycbwwIZjDvM3tJYgN85ILX7scB1qa5XTlWSaESOKLDh2mKNFvOBI/exec";  //HotFix 27/10/2015_1
//        String strJql = new String("project=CMRS and fixVersion = \"HotFix 27.10.2015_1\""); //фильтр для HotFix 27/10/2015_1
//        String sheetName = "Задачи HotFix"; // для HotFix 27/10/2015_1

//        String GoogleUri = "https://script.google.com/macros/s/AKfycbzNS78Vk1v2q9lkjHP5w7lo07RT-0lCZePw6WGOra7GXoENrXo/exec";  //релиза 30.11.2015
//        String strJql = new String("project=CMRS and fixVersion = \"reliz - 30.11.2015 - 2.2.2.0\""); //фильтр для релиза 30.11.2015
//       String sheetName = "Задачи 30.11.2015"; // для 30.11.2015
        String GoogleUri = args[3];
        String strJql = args[4];
        String sheetName = args[5];
        //SearchResult  searchResult = restClient.getSearchClient().searchJql(strJql, pm);
       final SearchResult searchResult = restClient.getSearchClient().searchJqlWithFullIssues(strJql,1000,0,pm);

        for (BasicIssue s : searchResult.getIssues()) {
            String issueKey = s.getKey();
            Issue issue = restClient.getIssueClient().getIssue(issueKey, pm);
            System.out.println(issueKey + "  -   " + issue.getStatus().getName());

            String issueType = issue.getIssueType().getName();
            String summary = issue.getSummary();
            String autor = issue.getReporter().getDisplayName();
            String priority = issue.getPriority().getName();
            String status = issue.getStatus().getName();

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("p1", sheetName));
            params.add(new BasicNameValuePair("p2", issueType));
            params.add(new BasicNameValuePair("p3", issueKey));
            params.add(new BasicNameValuePair("p4", summary));
            params.add(new BasicNameValuePair("p5", autor));
            params.add(new BasicNameValuePair("p6", priority));
            params.add(new BasicNameValuePair("p7", status));

            sendGET(GoogleUri, params);


        }
}


private static void sendGET(String URL, List<NameValuePair> query) throws IOException {

    URL obj = new URL(URL);
    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

    con.setRequestMethod("POST");
    con.setRequestProperty("Accept-Charset", "utf-8");

    con.setDoInput(true);
    con.setDoOutput(true);

    OutputStream os = con.getOutputStream();
    BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(os, "UTF-8"));
    writer.write(getQuery(query));
    writer.flush();
    writer.close();
    os.close();

    con.connect();

    int responseCode = con.getResponseCode();
   // System.out.println(con.getURL());
    System.out.println("GET Response Code :: " + responseCode);
    if (responseCode == HttpsURLConnection.HTTP_OK) { // success
        BufferedReader in = new BufferedReader(new InputStreamReader(
                con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // print result
        System.out.println(response.toString());
    } else {
        System.out.println("GET request not worked");
    }

}

    private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }


}




