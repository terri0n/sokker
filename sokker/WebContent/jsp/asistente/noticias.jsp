<style>
	p {
		margin-top: 5px;
		margin-bottom: 5px;
	}
	
	.fecha {
		font-weigth: 900;
		color: white;
		background-color: #539bcd;
		padding-left: 3px;
		padding-right: 3px;
		border-radius: 8px;
	}
	
	.bloque:nth-of-type(odd) {
		background-color: #ddeeffdd;
	}
	.bloque:nth-of-type(even) {
		background-color: #f8fcffdd;
	}
	
</style>

<div style="display: inline-block; max-width: 500px; margin-top: 10px;">
	<div class="cabecera" style="font-size: 1.5em;">LATEST NEWS</div>
	
	<div style="text-align: justify; max-height: 300px; overflow: auto;">
		<div class="bloque">
			<span class="fecha peque">2023-12-10</span>
			<b>Weight fixed</b>
			<p>The weight for the NT players was showing a value multiplied by 10 for some time. It has been fixed.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2023-10-16</span>
			<b>New icons set</b>
			<p>Due to the increasing use of the site, the old icons set free plan ran out of monthly uses, so I had to change them by a totally free new set. I've tried to choose the most similar icons :)</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2023-08-26</span>
			<b>Age factor for pace skill</b>
			<p>A new factor has been added in the configuration: <b>age factor for pace skill</b>. This is, the increment of time needed to train the pace skill as the age of your players increases.</p>
			<p>By default it takes the same value as the regular age factor, so no changes are produced if you don't change the configuration. But, in case you want to use it, the system will use this factor for the pace skill instead of the regular age factor.</p>
			<p>On the other hand, <b>the formula used to calculate the talent</b> was slightly modified. You may find that the talent of some of your players has changed a few tenths of a unit.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2023-05-27</span>
			<b>Test player</b>
			<p>A new button in the menu lets you add a test player. You can set his skills and add weeks of training to see how much talent the player has.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2023-04-08</span>
			<b>Skills projection updated</b>
			<p>Today I found that skills projection used a static 0.1 value of general training factor. I fixed it so that it uses the configured factor, which will considerably increase the projected skills.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2023-03-13</span>
			<b>New JSON api</b>
			<p>Some more new JSON api has been adapted and now you won't lose data if you forget to update your team some weeks (up to 10 weeks, in theory) since the new api provide that info. the only missing part is trainers changes.</p>
			<p>Also, new registered users will get old data from the moment they get registered.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2023-03-04</span>
			<b>General and formation factor reset</b>
			<p>Since many of you didn't manually change the general and formation training, it has been forced to 0.15 and 0.14 respectively for those who didn't make any change. I hope this will provide you better predictions.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2023-02-20</span>
			<b>Bug fixes</b>
			<p>A couple of bugs have been fixed:</p>
			<p><b>- Age in the end of the season.</b> On last Thursday of the season, the day before the players increased their age, <i>SA</i> stored the week with the old age, which caused that week to appear in the previous season and also showed the graphs wrongly.</p>
			<p><b>- Advanced training mark got lost</b> when the player properties were edited: notes, position, color...</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2023-02-11</span>
			<b>More changes in graphs and NT mode</b>
			<p>The graphs library has been completely replaced by a better one. I hope you like it :)</p>
			<p>Also, NT coaches can now get his players skills if you enter your Sokker login and password. Otherwise you will only get the public data as before.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2023-01-07</span>
			<b>Change in graphs</b>
			<p>Players training history graphs now show more clearly which weeks the player trained.</p>
			<p>I also wanted to remind you that sk-mail option for NT coaches stopped working. I can't tell anything else about it until I get an answer from the devs, sorry.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-11-27</span>
			<b>Massive export for NT coaches</b>
			<p>NT coaches can select and export their players to an external DB like NTDB.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-11-06</span>
			<b>Automatic updates</b>
			<p>Now you can choose this option to let <i>Sokker Asistente</i> update your team for you every Wednesday in the morning if you forget to update it during the week.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-10-29</span>
			<b>General and formation training factor updated</b>
			<p>According with the last developers diary, default general training factor has been increased by 50% and default formation training factor by 40%.</p>
			<p>Also a bug in some graphs that didn't show colors properly has been fixed. Thanks to kayalce for the warning :)</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-08-25</span>
			<b>Bug in player position and advanced training flag</b>
			<p>There was a bug when updating the team regarding the position of a player and the flag of "avanced training". That values were updated only in the current week, but not in the past.</p>
			<p>The bug has been fixed for the data retrieved from now on, but old data (from the new training system until now) could have been stored wrongly.</p>
			<p>Sorry for the inconvenience :/</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-08-17</span>
			<b>Skills near to pop</b>
			<p>Now skills that are near to pop (less than a full training) are marked in a different color. If it becomes annoying I will add a check in the configuration to disable it :)</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-08-03</span>
			<b>New actions</b>
			<p>Added actions to set a different position for the selected players.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-07-26</span>
			<b>New configurations</b>
			<p>Now you can configure <i>formation training factor</i>, which is initialized to 0.1 (10%) by default.</p>
			<p><i>General training factor</i> has also been reinitialized to the same value.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-07-23</span>
			<b>New training system issues</b>
			<p>The new training system changes caused some players to get no training points this week because they didn't store properly their training position. You can fix it manually by editing the affected trainings.</p>
			<p>Some users also reported that their training type was wrong this week. If that's your case, just change it in one player's last week training.</p>
			<p>These problems won't happen again since the new training system will be applied from this week on.</p>
			<p>Sorry for the inconvenience.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-07-09</span>
			<b>New tools for NT coaches</b>
			<p>NT coaches have a new button to hide those players that don't belong to the NT. They will also be able to remove massive players within a single action.</p>
			<p>More actions to come!</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-06-27</span>
			<b>Junior talent</b>
			<p>A new column has been added to the players: the junior talent. This is the talent (and graph) the player had when he was in the youth school, if he was our own junior.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-05-19</span>
			<b>Change of domain</b>
			<p><i>Sokker Asistente</i> is moving to a new server and, since I had to change the domain anyways, I did everything at once today.</p>
			<p>If you are using the Android app, you will need to install the new version.</p>
			<p>This is the only way I have to keep the tool free for everyone. Sorry for the inconvenience :)</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-03-29</span>
			<b>New league structure</b>
			<p><i>Sokker Asistente</i> has been adapted to the new league structure. Now, every player will only count the minutes played with the last order used right after a different order was used during the week.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-03-15</span>
			<b>Problems with Opera</b>
			<p>There is an issue connecting to Sokker with Opera browser. If you get an error message about CORS when updating your team and you have the CORS plug-in properly installed, try to use a different browser.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2022-02-13</span>
			<b>Automatic safe backups</b>
			<p>There won't be any more data loss since backups now are sent outside the server automatically (as long as the mail service works OK :P).</p>
			<p>In fact, this was done soon after the data loss accident, but I just realized I should mention it here :)</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2021-11-21</span>
			<b>Data loss</b>
			<p>Due to some problem with the server, the last 5-6 weeks of data have been lost.</p>
			<p>Remember that you can manually fix your training position of every week as well as your players pops. Now you can also fix your juniors levels.</p>
			<p>Sorry for the inconvenience.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2021-07-26</span>
			<b>Sk-mail tool for NT coaches</b>
			<p>Now you can send massive parameterized sk-mails to the owner of the desired players. Just select the players, click on "sk-mail" and fill in the popup form. Remember to update your players first!</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2021-05-17</span>
			<b>Solution for iPhone users</b>
			<p>Yandex Browser for <a href="https://play.google.com/store/apps/details?id=com.yandex.browser.alpha">Android</a> is confirmed to allow the installation of Chrome CORS extension. And it's also available for <a href="https://apps.apple.com/es/app/yandex-browser/id483693909">iPhone</a></p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2021-05-15</span>
			<b>NT players from SA users</b>
			<p>From now on, NT players coming from <i>Sokker Asistente</i> users will be updated directly into the NT database. The player will have the "reliable" flag marked since the skills come directly from <i>Sokker</i> xmls. Also, the flag "Accept players through an external form" won't be taken into account for these players.</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2021-05-01</span>
			<b>Android app and SETE available</b>
			<p>Android app with CORS is available and SETE now also uses the same CORS verification that Sokker Asistente</p>
		</div>

		<div class="bloque">
			<span class="fecha peque">2021-04-27</span>
			<b>Solution to server IP blocking</b>
			<p>To verify that your password is correct from your computer, now <i>Sokker Asistente</i> needs to connect directly with <i>Sokker</i>. This action, called CORS, by default is not allowed in browsers due to security reasons.</p>
			<p>However, there are several methods to disable it. Read the FAQ for more info.</p>
			<p><b>NOTE: the server IP is already unbloqued!</b></p>
		</div>

		<div class="fin_bloque impar">
			<span class="fecha peque">2021-04-09</span>
			<b>The password for <i>Sokker Asistente</i> will be the same that the one for <i>Sokker</i></b>
			<p>To reduce the number of failed attempts logging in to <i>Sokker</i> from the server, from now on, each time you update your team, the password used to connect to <i>Sokker</i> will be the password you will need to connect to <i>Sokker Asistente</i></p>
			<p>The process is the following:</p>
			<ol>
				<li>You login with your user/pass, as always</li>
				<li>You update your team with your <i>Sokker</i> user/pass, as always</li>
				<li>If the passwords are different, your <i>Sokker Asistente</i> password will be updated with your new <i>Sokker</i> password</li>
			</ol>
		</div>
	</div>
</div>