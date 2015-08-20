// SCRIPT.JS : script file for QuBE website
// By Mathijs Vogelzang, december 2007
// For questions: mathijs+qube@gmail.com

function showHeader(thispagename, name)
{
  document.write("<HEAD><TITLE>QuBE: Quantitative Blush Evaluation in acute myocardial infarction: " + name +
		 "</TITLE><LINK REL=\"stylesheet\" type=\"text/css\" href=\"style.css\">" +
	 	 "</HEAD>");

  document.write("<BODY><a href=\"https://github.com/mathijs81/qube\"><img style=\"position: absolute; top: 0; right: 0; border: 0;\" src=\"https://camo.githubusercontent.com/365986a132ccd6a44c23a9169022c0b5c890c387/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f7265645f6161303030302e706e67\" alt=\"Fork me on GitHub\" data-canonical-src=\"https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png\"></a>");

  document.write("<DIV class=\"document\"><center><TABLE width=\"80%\" cellpadding=\"0\"" +
		 "cellspacing=\"0\" border=\"0\">" +
		 "<TR height=\"auto\"><TD valign=\"top\" height=\"auto\">");

  document.write("<TABLE cellpadding=\"0\" cellspacing=\"0\" border=\"0\" WIDTH=200px>"+
		 "<TR><TD><div class=\"logo\">");
  showLogo();
  document.write("</div></TD></TR><TR><TD><div class=\"menu\">");
  showMenu(thispagename);
  document.write("</TD></TR><TR><TD><CENTER><BR><FONT COLOR=white>Hosted by:</FONT>");
  document.write("<BR><a href=\"http://sourceforge.net\"><img src=\"http://sflogo.sourceforge.net/sflogo.php?group_id=211159&amp;type=4\" width=\"125\" height=\"37\" border=\"0\" alt=\"SourceForge.net Logo\" /></a></CENTER></TD></TR>");
  document.write("</TABLE>");
  document.write("</TD><TD valign=top>");

  document.write("<DIV class=\"main\">");
}

function showMenuItem(naam, link, level, thispage)
{
  for(i=1;i<level;i++)
    document.write("&nbsp;&nbsp;");
  document.write("- ");
  if(thispage == link)
  {
    document.write("<FONT COLOR=black><I>" + naam + "</I></FONT>");
  }
  else
  {
    document.write("<A HREF=\"" + link + ".htm\">" + naam + "</A>");
  }
  document.write("<BR>");
}

function showMenu(thispagename)
{
  document.write("<B>Index</B><P>");
  showMenuItem("Main", "index", 1, thispagename);
  showMenuItem("Howto", "howto", 1, thispagename);
//  showMenuItem("Download", "download", 1, thispagename);
}

function showLogo()
{
  document.write("<CENTER><IMG SRC=\"http://bfiction.home.fmf.nl/qube.php\">");
 document.write("<BR><B><U>Q</U></B>uantitative <B><U>B</U></B>lush<BR>  <B><U>E</U></B>valuator</CENTER>");
}

function showFooter()
{
  document.write("<BR><SMALL><CENTER><HR WIDTH=\"30%\">Dept. of Cardiology, University Medical Center Groningen<BR>University of Groningen, the Netherlands<BR>Contact: <A HREF=\"mailto:mathijs+qube@gmail.com\">mathijs+qube@gmail.com</A></CENTER></SMALL></div></TD></TR></TABLE></center></DIV>");
  document.write("</BODY></HTML><DIV STYLE=\"display:none\">");
}
