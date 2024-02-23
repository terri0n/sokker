<%@page import="java.lang.reflect.Method"%>
<%@page import="java.lang.reflect.Field"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Enumeration"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Insert title here</title>
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
</head>
<body>

 	<script>
	 	$.post('https://sokker.org/start.php?session=xml', 'ilogin=&ipassword=').done(
	        function(data) {
	            try{
	                alert(data);
	            }catch (e){
	                alert(e);
	            }
	        } 
	    );   
	</script>

</body>
</html>