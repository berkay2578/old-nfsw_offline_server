package br.com.hosts;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

public class NfswSrv extends GzipHandler {

	private SocketClient socketClient = new SocketClient("localhost", 5333);
	private NfswBasket basket = new NfswBasket();
	private NfswCommerce commerce = new NfswCommerce();
	private NfswEvent event = new NfswEvent();
	private static Functions fx = new Functions();

	public static String modifiedTarget;
	public static boolean THBroken = false;
	private int iEvent = 0;
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		try {
			String sLastTarget = target.split("/")[target.split("/").length - 1];
			if (!sLastTarget.contains("cryptoticket") && !sLastTarget.contains("getrebroadcasters") && !sLastTarget.contains("heartbeat")) {
				//System.out.println(baseRequest.getMethod() + ": " + sLastTarget);
				fx.log(baseRequest.getMethod() + ": "+ target);
			}
			modifiedTarget = target;
			boolean isXmpp = false;
			
			if (target.matches("/nfsw/Engine.svc/User/SecureLoginPersona")) {
				Functions.personaId = baseRequest.getParameter("personaId");
				fx.ChangeDefaultPersona(String.valueOf((Integer.parseInt(baseRequest.getParameter("personaId")) / 100) - 1));
			}
			else if (target.matches("/nfsw/Engine.svc/setusersettings")) { fx.WriteText("../nfsw/Engine.svc/getusersettings.xml", new String(Files.readAllBytes(Paths.get("../nfsw/Engine.svc/getusersettings.xml")), StandardCharsets.UTF_8).replace("<starterPackApplied>false</starterPackApplied>", "<starterPackApplied>true</starterPackApplied>")); }
			else if (target.matches("/nfsw/Engine.svc/catalog/productsInCategory")) { modifiedTarget = target + "_" + baseRequest.getParameter("categoryName"); }
			else if (target.matches("/nfsw/Engine.svc/catalog/categories")) { modifiedTarget = target + "_" + baseRequest.getParameter("categoryName"); }
			else if (target.matches("/nfsw/Engine.svc/powerups/activated(.*)")) { isXmpp = true; event.processPowerup(sLastTarget, -1); }
			else if (target.matches("/nfsw/Engine.svc/matchmaking/joinqueueevent(.*)")) { iEvent = Integer.parseInt(sLastTarget); fx.log("|| Fake Event ID loaded: " + sLastTarget + ". Launch a singleplayer event to load it!");  /*sendXmpp(target);*/ }
			else if (target.matches("/nfsw/Engine.svc/matchmaking/launchevent(.*)")) { if (sLastTarget != String.valueOf(iEvent) && iEvent != 0) { modifiedTarget = "/nfsw/Engine.svc/matchmaking/launchevent/"+String.valueOf(iEvent); iEvent = 0; fx.log("|| -> Fake Event ID has been reset."); } }
			else if (target.matches("/nfsw/Engine.svc/badges/set")) fx.ChangeBadges(readInputStream(request));
			else if (target.matches("/nfsw/Engine.svc/personas/(.*)/baskets")) { modifiedTarget = "baskets"; basket.processBasket(readInputStream(request)); }
			else if (target.matches("/nfsw/Engine.svc/personas/(.*)/commerce")) { modifiedTarget = "commerce"; commerce.saveCommerceData(readInputStream(request)); }
			else if (target.matches("/nfsw/Engine.svc/personas/inventory/sell/(.*)")) { commerce.sell(sLastTarget, 0); }
			else if (target.matches("/nfsw/Engine.svc/personas/(.*)/defaultcar/(.*)")) { fx.ChangeCarIndex(target.split("/")[6], false); }
			else if (target.matches("/nfsw/Engine.svc/personas/(.*)/cars") && baseRequest.getMethod() == "POST") { basket.SellCar(baseRequest.getParameter("serialNumber")); }
			else if (target.matches("/nfsw/Engine.svc/personas/(.*)/carslots")) { fx.FixCarslots(); }
			else if (target.matches("/nfsw/Engine.svc/DriverPersona/GetPersonaInfo")) { modifiedTarget = target + "_" + Functions.personaId; }
			else if (target.matches("/nfsw/Engine.svc/DriverPersona/GetPersonaBaseFromList")) { modifiedTarget = target + "_" +  Functions.personaId; }
			else if (target.matches("/nfsw/Engine.svc/personas/inventory/objects")) { modifiedTarget = "/nfsw/Engine.svc/personas/" + Functions.personaId + "/objects"; }
			else if (target.matches("/nfsw/Engine.svc/events/notifycoincollected"))
			{
				fx.SaveTHProgress(baseRequest.getParameter("coins"));
				if(baseRequest.getParameter("coins").equals("32767")) // 15 maxcoins
				{
					System.out.println("|| Detected TH Finished event.");
					if (fx.GetIsTHStreakBroken().equals("true")) {
						THBroken = true;
						modifiedTarget = "THBroken";
						fx.log("|| -> Your TH Streak is broken.");
						Functions.answerData = "<Accolades xmlns=\"http://schemas.datacontract.org/2004/07/Victory.DataLayer.Serialization.Event\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><FinalRewards><Rep>25</Rep><Tokens>78</Tokens></FinalRewards><HasLeveledUp>false</HasLeveledUp><LuckyDrawInfo><Boxes><LuckyBox><CardDeck>LD_CARD_SILVER</CardDeck></LuckyBox><LuckyBox><CardDeck>LD_CARD_SILVER</CardDeck></LuckyBox><LuckyBox><CardDeck>LD_CARD_SILVER</CardDeck></LuckyBox><LuckyBox><CardDeck>LD_CARD_SILVER</CardDeck></LuckyBox><LuckyBox><CardDeck>LD_CARD_SILVER</CardDeck></LuckyBox></Boxes><CurrentStreak>" + String.valueOf(fx.GetTHStreak()) + "</CurrentStreak><IsStreakBroken>true</IsStreakBroken><Items></Items><NumBoxAnimations>100</NumBoxAnimations></LuckyDrawInfo><OriginalRewards><Rep>0</Rep><Tokens>0</Tokens></OriginalRewards><RewardInfo/></Accolades>";
					} else {
						event.ReadArbitration("<TreasureHunt/>");
						modifiedTarget = "THCompleted";
					}
				}
			}
			else if (target.matches("/nfsw/Engine.svc/event/arbitration")) {
				event.ReadArbitration(readInputStream(request)); 
				modifiedTarget = "Arbitration"; 
			}
			else if (target.matches("/nfsw/Engine.svc/events/accolades")) {
				if (THBroken) {
					fx.log("|| -> Your TH Streak will be revived for 1000 Boost.");
					event.ReadArbitration("<TreasureHunt/>");
					modifiedTarget = "THCompleted";
					THBroken = false; 
				}
			}
			else if (target.matches("/nfsw/Engine.svc/events/instancedaccolades")) { event.SetPrize(NfswEvent.RaceReward); modifiedTarget = "RaceReward"; }

			try {fx.log(readInputStream(request) + "\r\n");} catch(Exception e){}
			if (target.contains(".jpg")) { response.setContentType("image/jpeg"); } else { response.setContentType("application/xml;charset=utf-8"); }
			response.setStatus(HttpServletResponse.SC_OK);
			response.setHeader("Connection", "close");
			response.setHeader("Content-Encoding", "gzip");

			byte[] content = null;
			if (Files.exists(Paths.get(".." + modifiedTarget + ".xml"))) { content = Files.readAllBytes(Paths.get(".." + modifiedTarget + ".xml")); }
			else if (Files.exists(Paths.get(".." + modifiedTarget)) && !Files.isDirectory(Paths.get(".." + modifiedTarget))) { content = Files.readAllBytes(Paths.get(".." + modifiedTarget)); }
			else if (modifiedTarget != target) { content = Functions.answerData.getBytes(StandardCharsets.UTF_8); }

			if (content == null) { response.getOutputStream().println(); response.getOutputStream().flush(); }
			else {
				if (!target.contains(".jpg")) {
					String sContent = new String(content, StandardCharsets.UTF_8);
					if (sContent.contains("RELAYPERSONA")) sContent = sContent.replace("RELAYPERSONA", Functions.personaId);
					
					content = gzip(sContent.getBytes(StandardCharsets.UTF_8));
					response.setContentLength(content.length);

					response.getOutputStream().write(content);
					response.getOutputStream().flush();
				} 
				else
				{
					response.setContentLength(content.length);
					
					response.getOutputStream().write(content);
					response.getOutputStream().flush();
					response.getOutputStream().println();
					response.getOutputStream().flush();
				}
			}
			baseRequest.setHandled(true);
			if (isXmpp) sendXmpp(target);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String readInputStream(HttpServletRequest request) {
		StringBuilder buffer = new StringBuilder();
		try {
			BufferedReader reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}
	
	private byte[] gzip(byte[] data) throws IOException {
	  ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
	  try
	  {
		  OutputStream gzipout = new GZIPOutputStream(byteStream){{def.setLevel(1);}};
		  try
		  {
			  gzipout.write(data);
		  }
		  finally
		  {
			  gzipout.close();
		  }
	  	}
	  finally
	  {
		  byteStream.close();
	  }
	  return byteStream.toByteArray();
	}
	
	private void sendXmpp(String target) {
		try {
			String path = ".." + target + "_xmpp.xml";
			File fxmpp = new File(path);
			byte[] encoded = null;
			if (fxmpp.exists()) {
				encoded = Files.readAllBytes(Paths.get(path));
				if (encoded != null) {
					String msg = new String(encoded, StandardCharsets.UTF_8).replace("RELAYPERSONA", Functions.personaId);
					socketClient.send(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {		
			Locale newLocale = new Locale("en", "GB");
			Locale.setDefault(newLocale);
			// --------------------
			/*
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			//org.w3c.dom.Document doc = docBuilder.parse("..//nfsw/Engine.svc/catalog/productsInCategory_NFSW_NA_EP_PRESET_RIDES_ALL_Category.xml");
			Document doc = docBuilder.parse("../productsInCategory_NFSW_NA_EP_PRESET_RIDES_ALL_Category.xml");
			Document doc2 = docBuilder.parse("../nfsw/Engine.svc/catalog/productsInCategory_NFSW_NA_EP_PRESET_RIDES_ALL_Category.xml");
			
			String file = fx.ReadText("../nfsw/Engine.svc/catalog/productsInCategory_NFSW_NA_EP_PRESET_RIDES_ALL_Category.xml");
			int Amount = fx.CountInstances(file, "<ProductTrans>", "</ArrayOfProductTrans>");
			
			List<Integer> list = new ArrayList<Integer>();
			List<Integer> list2 = new ArrayList<Integer>();
			for (int i = 0; i < Amount; i++)
			{ 
				if (i < 323) {
					fx.log("-----------");
					fx.log(String.valueOf(i));
					fx.log("<Hash>"+doc.getElementsByTagName("Hash").item(i).getTextContent()+"</Hash>");
					fx.log("doc price: " + doc.getElementsByTagName("Price").item(i).getTextContent());
					///list.add(doc.getElementsByTagName("Hash").item(i).getTextContent());
					//list2.add(doc.getElementsByTagName("Price").item(i).getTextContent());
					
					int tillHashIndex = fx.CountInstances(file, "<ProductTrans>", "<Hash>"+doc.getElementsByTagName("Hash").item(i).getTextContent()+"</Hash>")-1;
					if (tillHashIndex >= 0) {
						fx.log("doc2 price: " + doc2.getElementsByTagName("Price").item(tillHashIndex).getTextContent());
						doc2.getElementsByTagName("Price").item(tillHashIndex).setTextContent(doc.getElementsByTagName("Price").item(i).getTextContent());
						doc2.getElementsByTagName("Icon").item(tillHashIndex).setTextContent(doc.getElementsByTagName("Icon").item(i).getTextContent());
						doc2.getElementsByTagName("Level").item(i).setTextContent("1234");
						
						list.add(tillHashIndex);
						
						if (doc.getElementsByTagName("Price").item(i).getTextContent().equals("2000000.0000")) {
							list2.add(tillHashIndex);
						}
					}
				}
			//list.add(doc.getElementsByTagName("ProductId").item(i).getTextContent()); 
			//list2.add("SRV-CAR" + String.valueOf(i)); 
				//<Price>0.0000</Price>
			//doc.getElementsByTagName("Price").item(i).setTextContent(String.valueOf(Integer.parseInt(docBuilder.parse("..//basket2/SRV-CAR" + String.valueOf(i) + ".xml").getElementsByTagName("ResalePrice").item(0).getTextContent())*2)+".0000"); 
			//Files.copy(Paths.get("..//basket/"+list.get(i).replace(":", "")+".xml"), Paths.get("..//basket2/"+list2.get(i)+".xml"), StandardCopyOption.REPLACE_EXISTING); 
			}
			/*fx.log("");
			fx.log("|| START DUMP");
			for (int i = 0; i < Amount; i++) {
				if (!list.contains(i)) {
					fx.log("Catalog Entry for Hash " + doc2.getElementsByTagName("Hash").item(i).getTextContent() + " {");
					fx.log("   Catalog Index: " + String.valueOf(i) + ",");
					fx.log("   Basket Id: " + doc2.getElementsByTagName("ProductId").item(i).getTextContent() + ",");
					fx.log("   Price: " + doc2.getElementsByTagName("Price").item(i).getTextContent() + ",");
					fx.log("   ERROR: \"This item couldn't be repriced. [NO DATA]\"");
					fx.log("};");
				} else {
					if (list2.contains(i)) {
						fx.log("Catalog Entry for Hash " + doc2.getElementsByTagName("Hash").item(i).getTextContent() + " {");
						fx.log("   Catalog Index: " + String.valueOf(i) + ",");
						fx.log("   Basket Id: " + doc2.getElementsByTagName("ProductId").item(i).getTextContent() + ",");
						fx.log("   Price: " + doc2.getElementsByTagName("Price").item(i).getTextContent() + ",");
						fx.log("   WARNING: \"Detected default price of 2,000,000.0000 IGC [CORRUPTED DATA]\"");
						fx.log("};");
					}
				}
			}
			
			fx.WriteXML(doc2, "../test.xml");
			file = fx.ReadText("../test.xml");
			
			for (int i = 0; i < 376; i++) {
				int index = fx.CountInstances(file, "<ProductTrans>", "<ProductId>SRV-CAR" + String.valueOf(i) + "</ProductId>")-1;
				int resaleprice = (int) Math.round(Double.parseDouble(doc2.getElementsByTagName("Price").item(index).getTextContent()) / 2);
				Document doc3 = docBuilder.parse("../dummy.xml");
				Document carDoc = docBuilder.parse("../basket/SRV-CAR" + String.valueOf(i) + ".xml");

				Node carTrans = doc3.importNode(carDoc.getFirstChild(), true);
				doc3.getElementsByTagName("CarSlotInfoTrans").item(0).appendChild(carTrans);
				
				fx.log("--------");
				fx.log("Car: SRV-CAR" + String.valueOf(i));
				fx.log("Catalog Price: "+ doc2.getElementsByTagName("Price").item(index).getTextContent());
				fx.log("Old Resale Price: " + carDoc.getElementsByTagName("ResalePrice").item(0).getTextContent());
				fx.log("New Resale Price: " + String.valueOf(resaleprice));
				
				doc3.getElementsByTagName("ResalePrice").item(0).setTextContent(String.valueOf(resaleprice));
				doc3.getElementsByTagName("OwnershipType").item(0).setTextContent("CustomizedCar");
				
				Node OwnedCar = doc3.getElementsByTagName("OwnedCarTrans").item(0);
				DOMImplementationLS lsImpl = (DOMImplementationLS)OwnedCar.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
				LSSerializer serializer = lsImpl.createLSSerializer();
				serializer.getDomConfig().setParameter("xml-declaration", false);
				String carBasket = serializer.writeToString(OwnedCar);
				
				fx.WriteText("../basket/SRV-CAR" + String.valueOf(i) + ".xml", carBasket);
			}
			fx.log("--------");
			fx.log("|| END DUMP");
			*/
			//-----------
			
			//fx.WriteXML(doc, "../nfsw/Engine.svc/catalog/productsInCategory_NFSW_NA_EP_PRESET_RIDES_ALL_Category.xml");
			/*Files.walk(Paths.get("..//nfsw"))
		     .filter(Files::isRegularFile)
		     .collect(Collectors.toList())
		     .forEach(path -> { 
		    	 try { if (path.toString().contains(".xml")) {
		    		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    		fx.WriteXML(docBuilder.parse(path.toString()), path.toString());
		    		
					System.out.println(path.toString());}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		     });
			Locale newLocale = new Locale("en", "GB");
			Locale.setDefault(newLocale);*/
			
			Server server = new Server(7331);
			server.setHandler(new NfswSrv());
			server.start();

			fx.log("");
			String THDate = fx.ReadText("serverSettings/THDate");
			if (THDate != LocalDate.now().toString()) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.ENGLISH);
				LocalDate lastCompletedTHDate = LocalDate.parse(THDate, formatter);
				LocalDate nowDate = LocalDate.now();
				long days = ChronoUnit.DAYS.between(lastCompletedTHDate, nowDate);
				
				fx.log("|| Last TH completed was on " + lastCompletedTHDate.toString() + ".");
				if (days == 0) {
					fx.log("|| -> Since that date is today, nothing will be done.");
				} else if (days == 1) {
					fx.StartNewTH(true);
				} else if (days >= 2) {
					fx.StartNewTH(false);
					fx.log("|| -> Since that date, it's been " + String.valueOf(days) + " days. Your TH Streak is broken.");
				} else { fx.log("|| !! -> Go back where you came from time traveller!"); }
			}
			
			String[] settings = Files.readAllLines(Paths.get("serverSettings/settings")).toArray(new String[]{});
			Functions.rewards = new int[] { Integer.parseInt(settings[1]), Integer.parseInt(settings[5]), Integer.parseInt(settings[6]), Integer.parseInt(settings[7]), Integer.parseInt(settings[8]) };
			Functions.multipliers = new double[] { Double.parseDouble(settings[2]), Double.parseDouble(settings[3]), Double.parseDouble(settings[4]) };
			Functions.rankDrop = new int[][] { new int[]{},  fx.StringArrayToIntArray(settings[10]), fx.StringArrayToIntArray(settings[11]), fx.StringArrayToIntArray(settings[12]), fx.StringArrayToIntArray(settings[13])} ;
			
			fx.WriteText("isRunning", "true");
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			fx.WriteText("isRunning", "error");
		}
	}
}