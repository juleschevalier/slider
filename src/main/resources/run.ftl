<html>
<head>
	<title>Reasoner Results</title>
</head>
<body>
	<TABLE BORDER="1" CELLPADDING="5">
		<TR>
			<TH>Session</TH>
			<TH>Date</TH>
			<TH>File</TH>
			<TH>Inference time</TH>
			<TH>Initial triples</TH>
			<TH>Inferred triples</TH>
			<TH>Loops</TH>
			<TH>Duplicates</TH>
			<TH>Missing triples</TH>
			<TH>Too triples</TH>
		</TR>
		<#list runs as run>
			<TR>
			${run}
			</TR>
		</#list>
	</TABLE>
</body>
</html>