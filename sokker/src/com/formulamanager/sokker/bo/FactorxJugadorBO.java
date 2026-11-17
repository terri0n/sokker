package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX;
import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.entity.factorx.JugadorFactorX;
import com.formulamanager.sokker.persistence.FactorxDAO;
import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.persistence.SqlitePersistence;
import com.formulamanager.sokker.entity.factorx.JugadorFactorX.DEMARCACION;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import java.net.URL;

/** Factor X player operations. Independent from Asistente's Jugador and BO. */
public final class FactorxJugadorBO {
    public static String PATH_BASE = System.getProperty("os.name").contains("Windows") ? "d:\\levi\\home\\" : "/home/";
    private FactorxJugadorBO() {}

    public static void setPathBase(String pathBase) {
        if (pathBase == null || pathBase.trim().isEmpty()) throw new IllegalArgumentException("Factor X path is required");
        PATH_BASE = pathBase.endsWith(File.separator) ? pathBase : pathBase + File.separator;
    }

    private static String getFormForo() {
        return "<form action=\"https://sokker.org/forum_topic/#new\" method=\"post\" name=\"newpost\" role=\"form\">" +
                "<input type=\"hidden\" name=\"pg\" value=\"0\"><input type=\"hidden\" name=\"post_no\" value=\"1\">" +
                "<input type=\"hidden\" name=\"ID_topic\" value=\"?\"><input type=\"hidden\" name=\"action\" value=\"Add\">" +
                "<textarea id=\"forum-topic-reply-text\" name=\"text\" rows=\"8\" class=\"form-control\"></textarea>" +
                "<button type=\"submit\" name=\"submit\" value=\"submit\" class=\"btn btn-primary\">Enviar</button></form>";
    }

    public static void escribir_anuncio(WebClient browser, JugadorFactorX player, boolean nuevo) {
        if (!player.isActualizado()) return;
        try {
            HtmlPage page=browser.getPage("https://sokker.org/player/PID/"+player.getPid());
            page=((HtmlAnchor)page.getFirstByXPath("//a[contains(@href,'forum_topic/ID_topic/')]")).click();
            Integer topicId=Integer.valueOf(page.getUrl().getPath().split("/")[3]);
            StringWebResponse response=new StringWebResponse(getFormForo(),new URL("file:///."));
            HtmlPage form=HTMLParser.parseHtml(response,browser.getCurrentWindow());
            ((HtmlInput)form.getElementByName("ID_topic")).setValueAttribute(topicId.toString());
            ((HtmlTextArea)form.getElementByName("text")).setTextContent(nuevo?"Añadido":"Actualizado");
            HtmlButton submit=form.getFormByName("newpost").getFirstByXPath("//button[@type='submit']");
            submit.click();
        } catch(Exception e) { e.printStackTrace(); }
    }

    public static int getJornadaMod(int round) { return round < 976 ? round % 16 : (round - 976) % 13; }

    private static FactorxDAO dao() throws IOException {
        File database=SqlitePersistence.getFactorxDatabaseFile();
        if(database==null)throw new IOException("Factor X SQLite is not initialized");
        return new FactorxDAO(new SqliteDatabase(database,false));
    }

    public static void grabar_jugadores(List<JugadorFactorX> players,TIPO_FACTORX type,int currentRound,boolean historical) throws IOException {
        try { dao().savePlayers(players,type,currentRound,historical); }
        catch(java.sql.SQLException e) { throw new IOException("Cannot save Factor X players for "+type,e); }
    }

    public static List<JugadorFactorX> leer_historico(TIPO_FACTORX type,int currentEdition) throws IOException {
        List<JugadorFactorX> result=new ArrayList<JugadorFactorX>();
        for(int edition=currentEdition;edition>23;edition--) result.addAll(leer_jugadores(type,edition,edition==currentEdition));
        return result;
    }

    public static List<JugadorFactorX> leer_jugadores(TIPO_FACTORX type,Integer edition,boolean current) throws IOException {
        try { return dao().loadPlayers(type,edition,current); }
        catch(java.sql.SQLException e) { throw new IOException("Cannot read Factor X players for "+type,e); }
    }

    public static boolean actualizar_jugador(WebClient browser,JugadorFactorX player,boolean force)
            throws FailingHttpStatusCodeException,MalformedURLException,IOException {
        HtmlPage page=browser.getPage("https://sokker.org/player/PID/"+player.getPid());
        SERVLET_FACTORX._log_linea(page.asXml());
        HtmlAnchor anchor=(HtmlAnchor)page.getFirstByXPath("//div[@class='panel-heading']//a[contains(@href,'player/PID/"+player.getPid()+"')]");
        if(anchor==null)return false;
        anchor.getTextContent();
        player.setNombre(anchor.getTextContent());
        player.setEdad(player.getEdad()==null?Integer.valueOf(anchor.getNextElementSibling().getTextContent())
                :Math.min(player.getEdad(),Integer.valueOf(anchor.getNextElementSibling().getTextContent())));
        anchor=(HtmlAnchor)page.getFirstByXPath("//a[contains(@href,'app/team/')]");
        if(anchor==null)player.setEquipo(null);
        else {
            player.setEquipo(anchor.getTextContent());
            player.setTid(Integer.valueOf(anchor.getAttributes().getNamedItem("href").getNodeValue().split("/")[2]));
        }
        anchor=(HtmlAnchor)page.getFirstByXPath("//div[@class='media-body']//a[contains(@href,'country/ID_country/')]");
        if(anchor!=null)player.setPais(Integer.valueOf(anchor.getHrefAttribute().split("/")[2]));
        HtmlSpan span=(HtmlSpan)page.getFirstByXPath("//li[contains(.,'value') or contains(.,'valor')]/span");
        if(span!=null)player.setValor(Integer.valueOf(span.getTextContent().replaceAll("\\D+","")));
        span=(HtmlSpan)page.getFirstByXPath("//td[contains(.,'stamina') or contains(.,'condición')]/strong/span[@class='skillNameNumber']");
        if(span==null)return force;
        player.setCondicion(Integer.valueOf(span.getTextContent().replaceAll("\\D+","")));
        player.setRapidez(skill(page,"pace","rapidez"));
        player.setTecnica(skill(page,"technique","técnica"));
        player.setPases(skill(page,"passing","pases"));
        player.setPorteria(skill(page,"keeper","portería"));
        player.setDefensa(skill(page,"defender","defensa"));
        player.setCreacion(skill(page,"playmaker","creación"));
        player.setAnotacion(skill(page,"striker","anotación"));
        return true;
    }

    private static Integer skill(HtmlPage page,String english,String spanish) {
        HtmlSpan span=(HtmlSpan)page.getFirstByXPath("//td[contains(.,'"+english+"') or contains(.,'"+spanish+"')]/strong/span[@class='skillNameNumber']");
        return Integer.valueOf(span.getTextContent().replaceAll("\\D+",""));
    }

    public static void poner_estrellas(List<JugadorFactorX> players,FactorxBO.FORMACIONES formation,TIPO_FACTORX type) {
        int[] starters={0,0,0,0}, substitutes={0,1,1,1};
        int reserveCount=0;
        if(type==TIPO_FACTORX.senior){starters=new int[]{1,4,3,3};substitutes=new int[]{1,1,1,1};reserveCount=4;}
        else if(players.size()>=20) {
            starters[0]=1;substitutes[0]=1;
            starters[1]=formation.name().charAt(1)-'0';starters[2]=formation.name().charAt(2)-'0';starters[3]=formation.name().charAt(3)-'0';
            if(players.size()>=40)reserveCount=3;
            else {
                if(starters[1]>3)starters[1]--;if(starters[2]>3)starters[2]--;if(starters[3]>2)starters[3]--;
                if(players.size()>=30)reserveCount=2;
                else {starters[1]--;starters[2]--;starters[3]--;reserveCount=1;}
            }
            int[] count=new int[4];for(JugadorFactorX p:players)count[p.getDemarcacion().ordinal()]++;
            if(count[1]<=5)starters[1]=1;else if(count[1]<10)starters[1]=2;
            if(count[2]<=5)starters[2]=1;else if(count[2]<10)starters[2]=2;
            if(count[3]<=5){starters[3]=1;substitutes[3]=0;}else if(count[3]<8)starters[3]=1;
        }
        int total=starters[0]+starters[1]+starters[2]+starters[3];
        for(JugadorFactorX p:players) {
            int position=p.getDemarcacion().ordinal();
            if(total>0&&starters[position]>0) {
                total--;starters[position]--;p.setIcono("<span class='estrella'>&#x2605;</span>");
                p.setIcono_foro("[/color][color=gold][size=16px]&#x2605;[/size][/color][/family][family=monospace][color="+p.getColor_demarcacion()+"]");
            } else if(reserveCount>0&&substitutes[position]>0) {
                reserveCount--;substitutes[position]--;p.setIcono("<span class='estrella_sup'>&#x2605;</span>");
                p.setIcono_foro("[/color][color=OliveDrab][size=16px]&#x2605;[/size][/color][/family][family=monospace][color="+p.getColor_demarcacion()+"]");
            }
        }
    }

    public static void actualizar_jugadores(WebClient browser,List<JugadorFactorX> players,int currentRound)
            throws FailingHttpStatusCodeException,MalformedURLException,IOException {
        Iterator<JugadorFactorX> iterator=players.iterator();
        while(iterator.hasNext()) {
            JugadorFactorX player=iterator.next();
            if(actualizar_jugador(browser,player,false)) {
                player.setActualizado(player.getJornada()==null||!player.getJornada().equals(currentRound));
                player.setJornada(currentRound);
            }
            if(player.getEquipo()==null)iterator.remove();
        }
    }

    public static void archivar_temporada(TIPO_FACTORX type,int currentRound) throws IOException {
        List<JugadorFactorX> players=leer_jugadores(type,FactorxBO.getEdicion(currentRound),true);
        grabar_jugadores(players,type,currentRound,true);
        List<JugadorFactorX> next=new ArrayList<JugadorFactorX>();
        for(JugadorFactorX p:players) {
            p.setEdad(p.getEdad()+1);
            if(p.validar(type)&&p.getJornadaMod()==0&&p.getPuntos().compareTo(BigDecimal.ZERO)>0) {
                p.setOriginal(new JugadorFactorX(p));p.setJornada(currentRound);next.add(p);
            }
        }
        grabar_jugadores(next,type,currentRound,false);
    }
}
