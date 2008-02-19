/*
 * Jalview - A Sequence Alignment Editor and Viewer
 * Copyright (C) 2007 AM Waterhouse, J Procter, G Barton, M Clamp, S Searle
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package jalview.io;

import java.io.*;

import java.awt.*;

import jalview.datamodel.*;
import jalview.gui.*;

public class HTMLOutput
{
  AlignViewport av;
  SequenceRenderer sr;
  FeatureRenderer fr;
  Color color;

  public HTMLOutput(AlignmentPanel ap, SequenceRenderer sr, FeatureRenderer fr1)
  {
    this.av = ap.av;
    this.sr = sr;

    fr = new FeatureRenderer(ap);
    fr.transferSettings(fr1);

    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty(
            "LAST_DIRECTORY"), new String[]
        {"html"},
        new String[]
        {"HTML files"}, "HTML files");

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle("Save as HTML");
    chooser.setToolTipText("Save");

    int value = chooser.showSaveDialog(null);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      jalview.bin.Cache.setProperty("LAST_DIRECTORY",
                                    chooser.getSelectedFile().getParent());

      try
      {
        PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(
            choice));
        out.println("<HTML>");
        out.println("<style type=\"text/css\">");
        out.println("<!--");
        out.print("td {font-family: \"" + av.getFont().getFamily() +
                  "\", \"" + av.getFont().getName() + "\", mono; " +
                  "font-size: " + av.getFont().getSize() + "px; ");

        if (av.getFont().getStyle() == Font.BOLD)
        {
          out.print("font-weight: BOLD; ");
        }

        if (av.getFont().getStyle() == Font.ITALIC)
        {
          out.print("font-style: italic; ");
        }

        out.println("text-align: center; }");

        out.println("-->");
        out.println("</style>");
        out.println("<BODY>");

        if (av.getWrapAlignment())
        {
          drawWrappedAlignment(out);
        }
        else
        {
          drawUnwrappedAlignment(out);
        }

        out.println("\n</body>\n</html>");
        out.close();
        jalview.util.BrowserLauncher.openURL("file:///" + choice);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  void drawUnwrappedAlignment(PrintWriter out)
  {
    out.println("<table border=\"1\"><tr><td>\n");
    out.println(
        "<table border=\"0\"  cellpadding=\"0\" cellspacing=\"0\">\n");

    //////////////
    SequenceI seq;
    AlignmentI alignment = av.getAlignment();

    // draws the top row, the measure rule
    out.println("<tr><td colspan=\"6\"></td>");

    int i = 0;

    for (i = 10; i < (alignment.getWidth() - 10); i += 10)
    {
      out.println("<td colspan=\"9\">" + i + "<br>|</td><td></td>");
    }

    out.println("<td colspan=\"3\"></td><td colspan=\"3\">" + i +
                "<br>|</td>");
    out.println("</tr>");

    for (i = 0; i < alignment.getHeight(); i++)
    {
      seq = alignment.getSequenceAt(i);

      String id = seq.getDisplayId(av.getShowJVSuffix());

      out.println("<tr><td nowrap>" + id +
                  "&nbsp;&nbsp;</td>");

      for (int res = 0; res < seq.getLength(); res++)
      {
        if (!jalview.util.Comparison.isGap(seq.getCharAt(res)))
        {
          color = sr.getResidueBoxColour(seq, res);

          color = fr.findFeatureColour(color, seq, res);
        }
        else
        {
          color = Color.white;
        }

        if (color.getRGB() < -1)
        {
          out.println("<td bgcolor=\"#" +
                      jalview.util.Format.getHexString(color) + "\">" +
                      seq.getCharAt(res) + "</td>");
        }
        else
        {
          out.println("<td>" + seq.getCharAt(res) + "</td>");
        }
      }

      out.println("</tr>");
    }

    //////////////
    out.println("</table>");
    out.println("</td></tr></table>");
  }

  void drawWrappedAlignment(PrintWriter out)
  {
    ////////////////////////////////////
    /// How many sequences and residues can we fit on a printable page?
    AlignmentI al = av.getAlignment();
    SequenceI seq;
    String r;
    String g;
    String b;

    out.println("<table border=\"1\"><tr><td>\n");
    out.println(
        "<table border=\"0\"  cellpadding=\"0\" cellspacing=\"0\">\n");

    for (int startRes = 0; startRes < al.getWidth();
         startRes += av.getWrappedWidth())
    {
      int endRes = startRes + av.getWrappedWidth();

      if (endRes > al.getWidth())
      {
        endRes = al.getWidth();
      }

      if (av.getScaleAboveWrapped())
      {
        out.println("<tr>");

        if (av.getScaleLeftWrapped())
        {
          out.println("<td colspan=\"7\">&nbsp;</td>");
        }
        else
        {
          out.println("<td colspan=\"6\">&nbsp;</td>");
        }

        for (int i = startRes + 10; i < endRes; i += 10)
        {
          out.println("<td colspan=\"9\">" + i + "<br>|</td><td></td>");
        }

        out.println("</tr>");
      }

      int startPos, endPos;
      for (int s = 0; s < al.getHeight(); s++)
      {
        out.println("<tr>");
        seq = al.getSequenceAt(s);

        startPos = seq.findPosition(startRes);
        endPos = seq.findPosition(endRes) - 1;

        String id = seq.getDisplayId(av.getShowJVSuffix());

        out.println("<td nowrap>" + id +
                    "&nbsp;&nbsp;</td>");

        if (av.getScaleLeftWrapped())
        {
          if (startPos > seq.getEnd() || endPos == 0)
          {
            out.println("<td nowrap>&nbsp;</td>");
          }
          else
          {
            out.println("<td nowrap>" + startPos +
                        "&nbsp;&nbsp;</td>");
          }
        }

        for (int res = startRes; res < endRes; res++)
        {
          if (!jalview.util.Comparison.isGap(seq.getCharAt(res)))
          {
            color = sr.getResidueBoxColour(seq, res);

            color = fr.findFeatureColour(color, seq, res);
          }
          else
          {
            color = Color.white;
          }

          if (color.getRGB() < -1)
          {
            out.println("<td bgcolor=\"#" +
                        jalview.util.Format.getHexString(color) + "\">" +
                        seq.getCharAt(res) + "</td>");
          }
          else
          {
            out.println("<td>" + seq.getCharAt(res) + "</td>");
          }

        }

        if (av.getScaleRightWrapped() &&
            endRes < startRes + av.getWrappedWidth())
        {
          out.println("<td colspan=\"" +
                      (startRes + av.getWrappedWidth() - endRes) + "\">"
                      + "&nbsp;&nbsp;</td>");
        }

        if (av.getScaleRightWrapped() && startPos < endPos)
        {
          out.println("<td nowrap>&nbsp;" + endPos +
                      "&nbsp;&nbsp;</td>");
        }

        out.println("</tr>");
      }

      if (endRes < al.getWidth())
      {
        out.println("<tr><td height=\"5\"></td></tr>");
      }
    }

    out.println("</table>");
    out.println("</table>");
  }

  public static String getImageMapHTML()
  {
    return new String(
        "<html>\n"
        + "<head>\n"
        + "<script language=\"JavaScript\">\n"
        + "var ns4 = document.layers;\n"
        + "var ns6 = document.getElementById && !document.all;\n"
        + "var ie4 = document.all;\n"
        + "offsetX = 0;\n"
        + "offsetY = 20;\n"
        + "var toolTipSTYLE=\"\";\n"
        + "function initToolTips()\n"
        + "{\n"
        + "  if(ns4||ns6||ie4)\n"
        + "  {\n"
        + "    if(ns4) toolTipSTYLE = document.toolTipLayer;\n"
        + "    else if(ns6) toolTipSTYLE = document.getElementById(\"toolTipLayer\").style;\n"
        + "    else if(ie4) toolTipSTYLE = document.all.toolTipLayer.style;\n"
        + "    if(ns4) document.captureEvents(Event.MOUSEMOVE);\n"
        + "    else\n"
        + "    {\n"
        + "      toolTipSTYLE.visibility = \"visible\";\n"
        + "      toolTipSTYLE.display = \"none\";\n"
        + "    }\n"
        + "    document.onmousemove = moveToMouseLoc;\n"
        + "  }\n"
        + "}\n"
        + "function toolTip(msg, fg, bg)\n"
        + "{\n"
        + "  if(toolTip.arguments.length < 1) // hide\n"
        + "  {\n"
        + "    if(ns4) toolTipSTYLE.visibility = \"hidden\";\n"
        + "    else toolTipSTYLE.display = \"none\";\n"
        + "  }\n"
        + "  else // show\n"
        + "  {\n"
        + "    if(!fg) fg = \"#555555\";\n"
        + "    if(!bg) bg = \"#FFFFFF\";\n"
        + "    var content =\n"
        + "    '<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\" bgcolor=\"' + fg + '\"><td>' +\n"
        + "    '<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\" bgcolor=\"' + bg + \n"
        +
        "    '\"><td align=\"center\"><font face=\"sans-serif\" color=\"' + fg +\n"
        + "    '\" size=\"-2\">&nbsp;' + msg +\n"
        + "    '&nbsp;</font></td></table></td></table>';\n"
        + "    if(ns4)\n"
        + "    {\n"
        + "      toolTipSTYLE.document.write(content);\n"
        + "      toolTipSTYLE.document.close();\n"
        + "      toolTipSTYLE.visibility = \"visible\";\n"
        + "    }\n"
        + "    if(ns6)\n"
        + "    {\n"
        +
        "      document.getElementById(\"toolTipLayer\").innerHTML = content;\n"
        + "      toolTipSTYLE.display='block'\n"
        + "    }\n"
        + "    if(ie4)\n"
        + "    {\n"
        + "      document.all(\"toolTipLayer\").innerHTML=content;\n"
        + "      toolTipSTYLE.display='block'\n"
        + "    }\n"
        + "  }\n"
        + "}\n"
        + "function moveToMouseLoc(e)\n"
        + "{\n"
        + "  if(ns4||ns6)\n"
        + "  {\n"
        + "    x = e.pageX;\n"
        + "    y = e.pageY;\n"
        + "  }\n"
        + "  else\n"
        + "  {\n"
        + "    x = event.x + document.body.scrollLeft;\n"
        + "    y = event.y + document.body.scrollTop;\n"
        + "  }\n"
        + "  toolTipSTYLE.left = x + offsetX;\n"
        + "  toolTipSTYLE.top = y + offsetY;\n"
        + "  return true;\n"
        + "}\n"
        + "</script>\n"
        + "</head>\n"
        + "<body>\n"
        + "<div id=\"toolTipLayer\" style=\"position:absolute; visibility: hidden\"></div>\n"
        + "<script language=\"JavaScript\"><!--\n"
        + "initToolTips(); //--></script>\n");

  }
}
