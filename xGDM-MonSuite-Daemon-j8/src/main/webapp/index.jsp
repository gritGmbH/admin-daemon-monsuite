<%--
  #%L
  xGDM-MonSuite Daemon deegree.PRO
  %%
  Copyright (C) 2022 - 2025 grit GmbH
  %%
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  #L%
  --%>
<%@ page contentType="text/html; charset=ISO-8859-1" %><%
%><%@page import="de.grit.gdi.daemon.utils.Utilities"%><%
%><%! 
private static String versionBase = Utilities.getPomVersion( Utilities.POM_BASE_GROUPID, Utilities.POM_BASE_ARTIFACTID );
private static String versionExtra = Utilities.getPomVersion( Utilities.getProp( "config.group", "de.grit.config" ),
                                                              Utilities.getProp( "config.artifact", "does_not_exist" ), null );
%><html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
		<meta name="author" content="grit GmbH, Stephan Reichhelm">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="imagetoolbar" content="no">
		<title>Monitoring Sensors</title>
		<style type="text/css">
body{font-family:verdana,arial,helvetica,sans-serif;
font-weight:normal;
font-size:10pt;
line-height:26pt;
color:#000;
background-color:#fff}
.border{border:1px #ccc solid}
table,tr{font-family:verdana,arial,helvetica,sans-serif;
font-weight:normal;
font-size:10pt}
		</style>
	</head>
	<body style="background-color:#f7f7f7;">
		<!-- $Revision: 12219 $ -->
		<div id="start_box" style="box-shadow: 8px 10px 30px grey; margin: 25px auto; width: 800px; height: 400px; border: 2px #ccc solid; background-color:#fff;">
			<table id="header" style="position:absolute; margin: 10px; padding: 0px; width:780px; height:34px; border-bottom: 1px #ccc solid;">
				<tbody>
					<tr>
						<td style="text-align: left; font-weight:bold; font-size:13pt" width="250px">Monitoring Sensors</td>
					</tr>
				</tbody>
			</table>
			<!--  -->
			<div id="center" style="position:absolute; margin-top: 60px; margin-left: -30px; width: 760px; height: 415px; border: 0px #0f0 solid;">
				<div style="position:absolute; top: 19px; margin-left: 520px; border:0px solid #ccc;">
					<img src="images/logo.png">
				</div>
			</div>
			<table id="footer" style="line-height:12pt; position:absolute; top:380px; margin-left: 10px; margin-right: 10px; margin-bottom: 10px; width:780px; height:25px; border-top: 1px #ccc solid;">
				<tbody>
					<tr>
						<td style="text-align: left; font-size:10pt" width="400px">
							Monitoring Sensors Version <%=versionBase%><%= versionExtra != null ? " (c" + versionExtra + ")" : "" %><br>
							powered by:
							<a href="https://www.deegree.pro/" target="_blank">deegree.PRO</a>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
</body>
</html>