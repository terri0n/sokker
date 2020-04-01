![Alt](/WebContent/img/sa.png "Sokker Asistente")

This is my first time in GitHub, so I might not use common standards, I'm afraid :P

I just uploaded my _Eclipse_ project with some applications appart from _Sokker Asistente_

To use _Sokker Asistente_ you will need to add the following variables in your _server.xml_ so that the app can login to Sokker and get, for example, NT players:
~~~~xml
  <GlobalNamingResources>
      <Environment name="login" value="..." type="java.lang.String" override="false"/>
      <Environment name="password" value="..." type="java.lang.String" override="false"/>
  </GlobalNamingResources>
~~~~

And that's all. Users will be created in /home/asistente in _.properties_ files (no DB needed)
