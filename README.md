This is my first time in GitHub, so I might not use common standards, I'm afraid :P

I just uploaded my Eclipse project with some applications appart from Sokker Asistente.

To use Sokker Asistente you will need to add the following variables in your server.xml so that the app can login to Sokker and get, for example, NT players:
~~~~
  <GlobalNamingResources>
      <Environment name="login" value="..." type="java.lang.String" override="false"/>
      <Environment name="password" value="..." type="java.lang.String" override="false"/>
  </GlobalNamingResources>
~~~~

And that's all. Users will be created in /home/asistente in .properties files (no DB needed)
