package com.formulamanager.sokker.acciones.sete;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Servlet implementation class Main
 */
@WebServlet("/oldsete")
public class OLD_SETE extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OLD_SETE() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			if (request.getServerPort() == 8080) {
				request.getSession().setAttribute("base", "http://localhost:8080/sokker/");
			} else {
				request.getSession().setAttribute("base", "https://raqueto.com/sokker/");
			}
			
			// T�cticas predefinidas
			List<Object[]> tacticas = dev_tacticas_predefinidas();
			request.setAttribute("tacticas", tacticas);

			if (request.getSession().getAttribute("ilogin") != null) {
				String ilogin = (String)request.getSession().getAttribute("ilogin");
				String ipassword = (String)request.getSession().getAttribute("ipassword");

				//Configuraci�n del navegador.
//				WebClient navegador = new WebClient();
//				navegador.getOptions().setJavaScriptEnabled(false);
//				navegador.getOptions().setCssEnabled(false);
//				navegador.getOptions().setUseInsecureSSL(true);
				
				//Proceso de login.
//				Login.hacer_login(navegador, ilogin, ipassword);
				
				new Navegador(false, ilogin, ipassword, request) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
						//----------
						// T�CTICAS
						//----------
						tacticas.add(null);
						tacticas.addAll(get_tacticas(navegador, null));

						//----------------
						// Seleccionador?
						//----------------
						HtmlPage pagina_principal = navegador.getPage(AsistenteBO.SOKKER_URL + "/team");

						HtmlDivision div = (HtmlDivision) pagina_principal.getFirstByXPath("//div[@class='panel-body']//div[@class=' ']/img[contains(@src,'/flags/')]/..");
						if (div != null) {
//							String flag = ((DomAttr)div.getFirstByXPath("img/@src")).getNodeValue();
							tacticas.add(null);
							Integer team_id = Integer.valueOf(((DomAttr)div.getFirstByXPath("a/@href")).getNodeValue().split("team/teamID/")[1]);
							tacticas.addAll(get_tacticas(navegador, team_id));
						}
					}
				};
			
//				navegador.close();
			}
			
			request.getRequestDispatcher("jsp/sete/oldsete.jsp").forward(request, response);
		} catch (FailingHttpStatusCodeException | LoginException | IOException | ParseException e) {
			e.printStackTrace();
			response.sendRedirect(request.getContextPath() + "/oldsete?error=" + e.getMessage());
		}
	}

	private List<Object[]> dev_tacticas_predefinidas() {
		List<Object[]> tacticas = new ArrayList<Object[]>();
		
		tacticas.add(new Object[] {"1", "Normal", "ADBCBEBDBECCCDDECECGDDDDDEEDFEFCFCEDECFEFCGCGDHCGEICICICIDHEJBJCJCICIDAFAFAGAHAHBEBGAGAGAHBGBGBGCGCGCECFCGDGDHDDDEDFEHEIEEEFEFFFFGHFHFHFIGIGAHAHAIAJAJAHAIAIBIBKCICICIBIBIDHDIDICJCKEGEHEIDKDLFIFJFJEJEKIIIIIIHJHJBKBLBKBMALCICKDKCLCMFKELDLDLDLFKEMELFMFMGKHMGLGMFMHKILIMIMIMILIMJMJMJNGBFCEDEEEFHBGBGAFBFEKBJAIBJBIDLAKAKBKCKCMALALBMCMENANBMBNDMDOANBODOEOFBDBFAHBIBICDCECGCIDJFCFEFGFHFIHDGEGFGHHIHDHEHFIHIIJDJEJFJHJILDLELFLGMHBGBGBHBJBLDFCGCICKCLFGFHFIFKFMHGGHGJGKHLIGIHIIHKHLJGJHJJJKJLMHLILJLKLLEJEKELFMGNFKFNGMGNHNILJNHMJOKNKMKMLOKOLOMKMMMOLOMOMLNLMONNNOOJOKOMNNOOEEEFEHEJEKGDGEGFGKGLJEIFJGIJJKKEJDKGJLKKLEKFLGKJLKNFNGMGNINJNENFNHNJNKHFHGHHHIHJIFIGIJIIIJKFKGKJKIKJLGLFLJLJLINFNGMININJOIOHNJOHOGPHPIOJPGPH"});
		tacticas.add(new Object[] {"7", "343", "AEAFAFAFAFBDBEBEBFBFCDCDCDCECEFBFCECEEFEFCFDFDGCGDHCGDGDGDGEJDIDIDIDIDAHAHAHAHAHAHAHAHAHAHBHCHBHCHBHDGDGDHDIDIEGEHEHEHEIFGFHFHFHFIHHHHHHHHHHBFBGBHBIBJCFCGCHCICJEEEFEHEJEKFEFFFHFJFKHEHFHHHJHKJEJFJHJJJKLHLHLHLHLHAJAJAJAJAKBJBJBKBKBLCKCKCLCLCLFKEKEMFMFNGLGMFLFLFMGKGLGLGLHMILILILILJLCBCCCDCECFDBDDDDECECFBFBFCGBGCIAIAIAIBICKBKDKBKCKCLBNBLCLDLDNBNDNDMDMEDFDGDHDIDJEGEHEHEHEIFGGGGHGIFIHEHFHHHJHKJDJFJHJJJLLELFLHLJLKMEMFNHMJMKEIEJEKEFEGGIGJGKGFGGIIJJJKJFIGKJKJKKKFKFMIMJMKMFMGNINJNJNFNGOIOIOIOGOGCJCKCLCMCNEMEMDLDLDNGMGNFMFNFNIMINIOIOIOKMKMKNKLKNLLLLLMNNLNMKMLNLNLNNECEDEEELEMGCGDGEGLGMJCJDJEJLJMKDKDKEKLKLMCMDMEMLMMNDNENFNKNLOFOGOGOIOJGFGGGHGIGJIFIGIHIIIJKFKGKHKIKJLGLGLHLILINFNGNHNINJOGOHOHOHOIPHPHPHPHPH"});
		tacticas.add(new Object[] {"8", "352", "AEAFAFAFAFBDBEBEBFBFCDCDCDCECEFBFCECEEFEFCFDFDGCGDHCGDGDGDGEJDIDIDIDIDAHAHAHAHAHAHAHAHAHAHBHCHBHCHBHDGDGDHDIDIEGEHEHEHEIFGFHFHFHFIHHHHHHHHHHBFBHBIBJBLDGDHDICJCKFGEHEIEKELGGGHGJGKGLIGIHHJHKHLKFJHJJJKJMLGLHLJLKLLAJAJAJAJAKBJBJBKBKBLCKCKCLCLCLFKEKEMFMFNGLGMFLFLFMGKGLGLGLHMILILILILJLCBCCCDCECFDBDDDDECECFBFBFCGBGCIAIAIAIBICKBKDKBKCKCLBNBLCLDLDNBNDNDMDMEBDBFBGBHBJCECFDGDHDIEDEEEGEHFIGDGEGFGHGIHDHEHFIHIIJCJEJFJHKJLDLELFLHLIDEDFCHDJDKEEFGEHFIEKHFHGHHHIHJJGJHJHJHJILFLGLHLILJMFMGMHMIMJNFNGNHNINJCJCKCLCMCNEMEMDLDLDNGMGNFMFNFNIMINIOIOIOKMKMKNKLKNLLLLLMNNLNMKMLNLNLNNECEEEFFGFIHCHEHFIFIHJCJDJEKGKHKDKELELFLGMDMENFNGNHOFOFOGOHOHPFPGPGPGPHFGFIFJEKEMIHIJIJHKHMKHKIKKJLJMLILJLKKKKLNHNINJMKMLOHOHOIOJOJPHPIPIPIPJ"});
		tacticas.add(new Object[] {"9", "424", "ABBCBDBDBECBCCCCCDCEDBDBDCEDEDFBFCECECFDGBGCGCHCGDIBICICIDHDJBJCJCICICAFAFAGAHAHBEBGAGAGAHBGBGBGCGCGCECFCGDGDHDDDEDFEHEIEEEFEFFFFGHFHFHFIGIGAHAHAIAJAJAHAIAIBIBKCICICIBIBIDHDIDICJCKEGEHEIDKDLFIFJFJEJEKIIIIIIHJHJBKBLBLBMANCKCLCMCMCNELELDMDNDNFLEMEMFMFNGLHMGMGMGNHLILIMIMINIMIMJMJMJNGBFCEDEEEFHBGBGCFDFEKBJBIBJCIDLAKAKBKCKDMALBLBMCMENANBMCMEMEOANCODOEOFBDBFAHBIBICDCECGCIDJFCFEFGFHFIHDGEGFGHHIHDHEHFIHIIJDJEJFJHJILDLELFLGMHBGBGBHBJBLDFCGCICKCLFGFHFIFKFMHGGHGJGKHLIGIHHJHKHLJGJHJJJKJLMHLILJLKLLEJEKELFMGNFKFLGMGNHNILJMINJNKNKLKMKNKOLOMKMMLNLNMOMKMKMMNNNOOJOKOLNMOOEEEFEHEJEKGDGEGHGKGLJEJFJHJJJKKEKGKHKIKKLEMFLHMJLKNFNGMHNINJNENFNHNJNKHFHGHHHIHJIFIGIHIIIJKFKGKHKIKJLGLGLHLILINFNGMHNINJOIOHNHOHOGPHPIPHPGPH"});
		tacticas.add(new Object[] {"10", "433", "ABBCBDBDCEDBCCCCCDCEEBEBECEDEDHBFCECECFDHBHCGCHCGDJBJCICIDHDKBKCKDICIBAFAFAHAHAHBEBGAGAGAHBGBGBGCGCGCEDICGDGDHDHDEDFDGDHEEEFEFEFEGHFHFHFHFHFAHAHAIAJAJAHAIBIBIBKCICICIBIBIDHCFDICJCKEEEHEIEJEKFIFJFJFJFKIIIIIIIIIICKBLBLBMANCKCLCMCMDNELELEMENENFLEMEMFMHNGLHMGMHMHNHLILIMJMJNIMIMKLKMKNDCDEDFDGEFGBECFCEDEEHBGBHBHCHDJBJAIBICJDKBKDKBKCKCLBNBLCLDLDNBNDNDNDMDCFBGBHBICJDFCFCHCJDJEEEFEHEJEKHEHFGHHJHKIEJFIHJJIKKEKFKHKJKKMFMGMHMIMJFJFKFLFMFNHIHJHKHLHMIIIJIKILIMKJKDKKKLKLMCMJMKMLMMNINJNJNKNLOIOIOIOJOKEJDIDJDKDLEKELFMEMGNHLHMHNGNHNJLIMINJOJNKMKMKNKLKNLLLLLMNNLNMLNLNLNLNNFBFCFDFEFFHCHDHEHFHGICIDIEIFIGKDKJKEKFKFMIMDMEMFMGNDNENFNFNGOEOFOGOGOGHFHGHHHIHJIFIGIHIIIJKFKGKHKIKJLGLGLHLILINFNGNHNINJOGOHOHOHOIPHPHPHPHPH"});
		tacticas.add(new Object[] {"11", "442", "ABBCBDBDCEDBCCCCCDCEEBEBECEDEDHBFCECECFDHBHCGCHCGDJBJCICIDHDKBKCKDICIBAFAFAHAHAJBEBGAGAGAHBGBGBGCGCGCECFCGDGDHDHDEDFDGDHEEEFEFEFEGHFHFHFHFHFAHAHAIAJAHAHAIBIBIBKCICICIBIBIDHDIDICJCKEEEHEIEJEKFIFJFJFJFKIIIIIIIIIICKBLBLBMANCKCLCMCMDNELELEMENENFLEMEMFMHNGLHMGMHMHNHLILIMJMJNINIMKLKMKNDDDEEEEEEFGBECFCEDEEIBGBHBHCHDJBJAIBICJDKBKDKBKCKCLBNBLCLDLDNBNDNDNDMDBFBHAGBHBJCFCFBHCJCJDEDFDHDJDKFEFFFHFJFKHEHFHHHJHKJEJGJHJIJKKELFLHLJKKEFEGDHEIEJEFEGEHEIEJFFGGGHGIFJIFIGHHIIIJKEKGJHKIKKLFLELHLKLJNINHNHNHNGEJEKEKDKDLEKELFMEMGNHLHMHNGNINJLIMINJOJNKMKMKNKLKNLLLLLMNNLNMLNLNLNLNNECEEEFFGFIHCHEHFIFIHJCJDJEKGKHKDKELELFLGMDMENFNGNHOFOFOGOHOHPFPGPGPGPHFGFIFJEKEMIHIJIJHKHMKHKIKKJLJMLILJLKKKKLNHNINJMKMLOHOHOIOJOJPHPIPIPIPJ"});
		tacticas.add(new Object[] {"12", "451", "ABBCBDBDCEDBCCCCCDCEDBDBDCEDEDHBFCECECFDHBHCGCHCGDJBJCICIDHDKBKCKDICICAFAFAHAHAHBEBGAGAGAHBGBGBGCGCGCECFCGDGDHDDDEDFEHEIEEEFEFFFFGHFHFHFIGIGAHAHAIAJAJAHAIBIBIBKCICICIBIBIDHDIDICJCKEGEHEIDKDLFIFJFJEJEKIIIIIIHJHJCKBLBLBMANCKCLCMCMDNELELDMDNDNFLEMEMFMHNGLHMGMHMHNHLILIMJMJNIMIMKLKMKNEBEDEEEEEFGBECFCEDEEIBGBHBHCHDKBJAIBICJDMALBKBKCKCNANBLCLDLDOANCMDNDMDBFBHAGBHBJCFCFBHCJCJDEDFEHDJDKFEFFFHFJFKHEHFHHHJHKJEJGJHJIJKLFLFLHLJLJDHDHCICJCKEGEHEJEKELFGFHGJGKGLIGIHJJJKJLKHKIKJKKKLLGLHLJLKLLNHNINJNJNKEJEKEKELENEKELFMEMGNHLHMHNGNINJLIMINJOKNKMKMKNLNMOLLLLLMNNNOMLNLMLNMOOCECFCGDHDHEDEEEFEHEIGDGEGFFHFIJDJEJFIHIIKDKEKFKGKHLDLELFLHLINENFNFNGNHHGHHHHHHHIIGIGIHIIIIKFKGKHKIKJLGLGLHLILIMGNGMHNIMINHNINHNGNHOHOHOHOHOH"});
		tacticas.add(new Object[] {"13", "523", "ABBCBEBEAFCABCBDBECEEBDBDBDDDDGAGBFBGCGCJAIBHBICIELAKBKCKDKEMALBLCLDLEAFAGAHAIAJAGAGAHAIAIBFBGBHBIBJEFEGDHEIEJGFGGGHGIGJIFIGIHIIIJKFKGKHKIKJAHAIAIAJBLBHBHBIBKCLCHCICJCKCLFIFIEJDKDLFIFIFJFKFLHIHJHKHLHMIIIJIKILIMAJBKBKBMANCKBKBLBMCODLDLDNDNENGMGMFNGNGOIKIMHNINJOKKKLKMKNLOLKLLLMLNMOFAEBDDDEDFGAFBFDFDFEJAIBICIDIEKAKAKCKDKEMCMBMDMEMFNDNENENFNFODOEOEOGOFBDAFAGAGAHCDBEBGBHBHCDCECFCGCHDDDEEFFGFGFDFEFFFGFGHCHDHEHFHGICIDIEIFIGCFCGBIBJBKDGDHDIDKDLFFFHFJFLFMHFHGHJHLHMJFJGJJJLJMLGLHLJLKLMNGNHNIMJMKDJDKDLENFOFKFLFLFNGOIKILIMINJOKKKLKMKOKOMJMKMLMNMMNJNJNKNKNLOJOIOKOKOLBEBFBGCICJDDDEDGDHDIFCFDFFFHFJHCHDHFHIHJJCJDJFJIJJLCLELFLHLIMEMFNGNHNIFEFGFHFIFKIEIGIHIIIKKEKFKHKJKKLFLGLHLILJNFNGNHNINJOGOGOHOIOIPHPGOHPIPH"});
		tacticas.add(new Object[] {"14", "532", "AEAFAFAFAFBDBEBEBFBFCDCDCDCECEFBFCECEEFEFCFDFDGCGDHCGDGDGDGEJDIDIDIDIDAHAHAHAHAHAHAHAHAHAHBHCHBHCHBHDGDGDHDIDIEGEHEHEHEIFGFHFHFHFIHHHHHHHHHHBFBHBIBJBLDGDHDICJCKFGEHEIEKELGGGHGJGKGLIGIHHJHKHLKFJHJJJKJMLGLHLJLKLLAJAJAJAJAKBJBJBKBKBLCKCKCLCLCLFKEKEMFMFNGLGMFLFLFMGKGLGLGLHMILILILILJLCBCCCDCECFDBDDDDECECFBFBFCGBGCIAIAIAIBICKBKDKBKCKCLBNBLCLDLDNBNDNDMDMEBDBFBGBHBJCECFDGDHDIEDEEEGEHFIGDGEGFGHGIHDHEHFIHIIJCJEJFJHKJLDLELFLHLIDEDFCHDJDKEEFGEHFIEKHFHGHHHIHJJGJHJHJHJILFLGLHLILJMFMGMHMIMJNFNGNHNINJCJCKCLCMCNEMEMDLDLDNGMGNFMFNFNIMINIOIOIOKMKMKNKLKNLLLLLMNNLNMKMLNLNLNNECEEEFFGFIHCHEHFIFIHJCJDJEKGKHKDKELELFLGMDMENFNGNHOFOFOGOHOHPFPGPGPGPHFGFIFJEKEMIHIJIJHKHMKHKIKKJLJMLILJLKKKKLNHNINJMKMLOHOHOIOJOJPHPIPIPIPJ"});
		tacticas.add(new Object[] {"15", "541", "ABBCBEBEAFCABCBDBECEEBDBDBDDDDGAGBFBGCGCJAIBHBICIELAKBKCKDKEMALBLCLDLEAFAGAHAIAJAGAGAHAIAIBFBGBHBIBJEFEGDHEIEJGFGGGHGIGJIFIGIHIIIJKFKGKHKIKJAHAIAIAJBLBHBHBIBKCLCHCICJCKCLFIFIEJDKDLFIFIFJFKFLHIHJHKHLHMIIIJIKILIMAJBKBKBMANCKBKBLBMCODLDLDNDNENGMGMFNGNGOIKIMHNINJOKKKLKMKNLOLKLLLMLNMODBDCDDDEDEFBECEDECFCHAGBGCGBHDJAJAJAJBJCMBMBLBKCKCOBNBNCMDMDNBNDNDNDNEBDAFAGAGAHCDBEBGBHBHCDCECFCGCHDDDEEFFGFGFDFEFFFGFGHCHDHEHFHGICIDIEIFIGCFBFBHBJCJDEDFDHDJDKFDFFFHFJFLGEGFHHGJGKIDJFJHJJILKDKELHKKKLMDMFNHMJMLDKDKDLDMDNFMEMELEMFNHLGNGMGNHOJMJNJOJOJOKMKMLNMNMNMLMLNMNNONNKNLNLNLNNCDCFCHCJCLFEFGFHFIFKIFHGHHHIIJJEJFJHJJJKLELGLHLILKNFMFNGMJNJOFOGPGOIOJFEFGFHFIFKIEIGIHIIIKKEKFKHKJKKLFLGLHLILJNGNHNHNHNIOHOGOIOIOHPHPIPIPGPH"});
		tacticas.add(new Object[] {"16", "Attack", "AEAFAFAFAFBDBEBEBFBFCDCDCDCECEFBFCECEEFEFCFDFDGCGDHCGDGDGDGEJDIDIDIDIDAHAHAHAHAHAHAHAHAHAHBHCHBHCHBHDGDGDHDIDIEGEHEHEHEIFGFHFHFHFIHHHHHHHHHHBDBFBGBHBJCECFDGDHDIECEEEGEHFIGDGEGFGHGIHDHEHFIHIIKCKEJFJHJJMDMELFLHLIAJAJAJAJAKBJBJBKBKBLCKCKCLCLCLFKEKEMFMFNGLGMFLFLFMGKGLGLGLHMILILILILJLEAEBECEEEFHAHBGCGCGDKAJBIBJBJCLAKAKBKBLCMDMCMBMCMDNBNBNDNENFNBODNEOEOFBFBHBIBJBLDGDHDICJCKFGEHEIEKEMGGGHGJGKGLIGIHHJHKHLJFJHJJKKKMLGLHMJMKMLDEDFDHDJDKFEFGFHFIFKIFIGHHIIIJKFKGJHKIKJLFLGLHLILJMFNGNHNIMJNGNGOHNINIEJEKEMENEOGLGMGMHNHOJMJNINJNKOLMKNKNKOLOMLMMMNMMMLNJNKNLNNNNOJOKNKOLNNGCFEFFGGGIICIEHFJGJHKCKDJFLGLHMDMELFLFLGNENFNGNGOHOFOFPGPHOHPFPGPGPGPHGGGIGJFKGMJHJIIJIKIMLHLIKJKLKMLILJMJMKMLOHNIOINJNKOHPHPIOJOJPHPIPIPIPJ"});
		tacticas.add(new Object[] {"17", "Defend", "ADBFAGBFAGBDBEBEBEBFCDCECECEBFDDDEDEDECFFDFEFEFEFEFDFDFDFDFDHDHDHDHDHDAGAHAHAGAHAFAGAGAGAHBFBGBGBGAHCFCGCGCGCHEFEGEGEGEGEGEGEGEGEGGGGGGGGGGGAHAIBHAHAIAHAIAIAIAJAHBIBIBIBJCHCICICICJEIEIEIEIEJEIEIEIEIEIGIGIGIGIGIAIBJAIBJALBJBKBKBKBLBJCKCKCKCLCJDKDKDKDLFKFKFKFKFLFLFLFLFLFLHLHLHLHLHLBCBDBDBDBDCBCCCCCCCDDBDCDCDCCDEBECECECDDGBGCGCGCGCHBHBHBHBHBJBJBJBJBJBBFAGBGBGBGBEBFCGCHCIECEEEFEGEHFCFEFFFGFGHDHEHFHGHHIDIEIFJGJHKEKEKFLGMHBIBIBIAIBJCGCHCIBJBKEHEIEJEKEMFIFIFJFKFMHHHIHJHKHLJHJIIJIKILMHLIKJKKKKBLBLBLBLBMCLCMCMCMCNCLDMDMDMDNDLEMEMEMENGMGMGMGMGNHNHNHNHNHNJNJNJNJNJNCGCHCHCHCIEDEFEHEJELGDGGGHGIGLIEIFIHIJIKJDJFJHJJJLLBLELHLKLNNBNEMHNKNNHGHHHHHHHIIFIGIHIIIJKFKGKHKIKJLGLGLHLILIMFMGMHMIMJOHOGNHOIOHPHPGOHPIPH"});
		tacticas.add(new Object[] {"18", "Sweep", "ABBCBDBDCEDBCCCCCDCEEBEBECEDEDGBFCECECFDFBFCFCFCFDGBGCGCGDGDHCHCHCHCHCAFAFAHAHAJBEBGAGAGAHBGBGBGCGCGDHCFCGDGDHFFEGEGEHFJIGHGHHHIIIKEKFKGKIKKAHAHAIAJAHAHAIBIBIBKCICICIBIBICEDIDICJCKDGDHDIDIDIEHEHEHEHEHGHGHGHGHGHCKBLBLBMANCKCLCMCMDNELELEMENENFLEMEMFMGNFLFMFMFMFNGLGLGMGMGNHMHMHMHMHMDDDEEEEEEFGBECFCEDEEIBGBHBHCHDJBJAIBICJDJBKDKBKCKCLBNBLCLDLDNBNDNDNDMDBFBHAGBHBJCFCFBHCJCJDEDFDHDJDKFEFFFHFJFKHEHFHHHJHKJEJGJHJIJKLGLHLHLHLIEFEGDHEIEJEFEGEHEIEJFFGGGHGIFJIFIGHHIIIJJEKGJHKIJKLFLELHLKLJNINHNHNHNGEJEKEKDKDLEKELFMEMGNHLHMHNGNINJLIMINJOJNKMKMKNKLJNLLLLLMNNLNMLNLNLNLNNECEEEFFGFIHCHEHFIFIHJCJDJEKGKHKDKELELFLGMDMENFNGNHOFOFOGOHOHPFPGPGPGPHFGFIFJEKEMIHIJIJHKHMKHKIKKJLJMLILJLKKKKLNHNINJMKMLOHOHOIOJOJPHPIPIPIPJ"});
		
		return tacticas;
	}
	
	private List<Object[]> get_tacticas(WebClient navegador, Integer team_id) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/tactedit/teamID/" + (team_id == null ? "" : team_id));
//System.out.println(pagina.asXml());
		List<HtmlAnchor> htmlTacticas = (List<HtmlAnchor>)pagina.getByXPath("//div[@class='panel-body']//a[contains(@href,'teamID') and contains(@href,'tactedit')]");
		List<Object[]> tacticas = new ArrayList<Object[]>();

		for (HtmlAnchor t : htmlTacticas) {
			String href = t.getAttributes().getNamedItem("href").getNodeValue();
			Integer id = Integer.valueOf(href.substring(href.indexOf("tactID") + 7, href.indexOf("#")));
			HtmlPage paginaTactica = navegador.getPage(AsistenteBO.SOKKER_URL + "/read_tact.php?id=" + id);
			String tactica = paginaTactica.asText().split("&")[0].split("=")[1];

			if (id > 18) {
				tacticas.add(new Object[]{id, t.asText(), tactica});
			}
		}
		
		return tacticas;
	}
}
