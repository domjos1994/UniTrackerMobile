<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C)  2019-2020 Domjos
  ~  This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
  ~
  ~  UniTrackerMobile is free software: you can redistribute it and/or modify
  ~  it under the terms of the GNU General Public License as published by
  ~  the Free Software Foundation, either version 3 of the License, or
  ~  (at your option) any later version.
  ~
  ~  UniTrackerMobile is distributed in the hope that it will be useful,
  ~  but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~  GNU General Public License for more details.
  ~
  ~  You should have received a copy of the GNU General Public License
  ~  along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
	  <html>
		  <body>
			<h2>Projects</h2>
			<table border="1">
				<tr bgcolor="#9acd32">
				  <th>Title</th>
				  <th>Enabled</th>
				</tr>
				<xsl:for-each select="Projects/Project">
					<tr>
					  <td><xsl:value-of select="@title"/></td>
					  <td><xsl:value-of select="@enabled"/></td>
					</tr>
				</xsl:for-each>
		  </table>
		  </body>
	  </html>
	</xsl:template>
</xsl:stylesheet> 