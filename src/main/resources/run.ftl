<html>
<head>
	<title>Reasoner Results</title>
</head>
<body>
	<TABLE BORDER="1" CELLPADDING="5">
		<TR>
			<TH>File</TH>
			<TH>Input</TH>
			<TH>Inferred</TH>
			<TH>Threads</TH>
			<TH>Buffer</TH>
			<TH>Inference Time</TH>
			<TH>Parsing Time</TH>
		</TR>
		<#list runs as run>
			<TR>
			${run}
			</TR>
		</#list>
	</TABLE>
</body>
</html>