![Sokker Asistente](/WebContent/img/sa.png "Sokker Asistente")

[Sokker Asistente](http://www.formulamanager.com/sokker/asistente)

This is my first time in _GitHub_, so I might not use common standards, I'm sorry :P

I just uploaded my _Eclipse_ project with some applications appart from _Sokker Asistente_

To use _Sokker Asistente_ you will need to add the following variables to your `server.xml` so that the app can login to Sokker and get, for example, NT players:
````xml
  <GlobalNamingResources>
      <Environment name="login" value="..." type="java.lang.String" override="false"/>
      <Environment name="password" value="..." type="java.lang.String" override="false"/>
  </GlobalNamingResources>
````

And that's all. Users will be created in `/home/asistente` in `.properties` files (no DB needed!)
