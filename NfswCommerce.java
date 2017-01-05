package br.com.hosts;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NfswCommerce {
	
	private Functions fx = new Functions();
	
	public void saveCommerceData(String commerceTrans) {
		try {
			fx.log(commerceTrans + "\r\n");
			fx.log("|| Commerce action detected.");
			Integer carId = Integer.parseInt(fx.ReadCarIndex());

			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc2 = docBuilder.parse(new InputSource(new StringReader(commerceTrans)));

			fx.log("  || Initializing economy module.");
			NfswEconomy economy = new NfswEconomy();
			
			sell(commerceTrans, 1);
			
			int Occur = fx.CountInstances(commerceTrans, "<ProductId>", "<UpdatedCar>");
			fx.log("  || -> " + String.valueOf(Occur) + " part(s) to be purchased.");
			for (int i = 0; i < Occur; i++) {
				String productId = doc2.getElementsByTagName("ProductId").item(i).getTextContent();
				for (int j = 0; j < Integer.parseInt(doc2.getElementsByTagName("Quantity").item(i).getTextContent()); j++) {
					economy.AddCommerce(mapProductId(productId), productId);
				}
			}
			if (economy.DoCommerce()) {
				Document doc = docBuilder.parse("../nfsw/Engine.svc/personas/"+Functions.personaId+"/carslots.xml");
				
				Node Paints = doc.importNode(doc2.getElementsByTagName("Paints").item(0), true);
				Node PerformanceParts = doc.importNode(doc2.getElementsByTagName("PerformanceParts").item(0), true);
				Node SkillModParts = doc.importNode(doc2.getElementsByTagName("SkillModParts").item(0), true);
				Node Vinyls = doc.importNode(doc2.getElementsByTagName("Vinyls").item(0), true);
				Node VisualParts = doc.importNode(doc2.getElementsByTagName("VisualParts").item(0), true);
				fx.log("|| -> Loaded new parts into memory.");
				Node oldPaints = doc.getElementsByTagName("Paints").item(carId);
				Node oldPerformanceParts = doc.getElementsByTagName("PerformanceParts").item(carId);
				Node oldSkillModParts = doc.getElementsByTagName("SkillModParts").item(carId);
				Node oldVinyls = doc.getElementsByTagName("Vinyls").item(carId);
				Node oldVisualParts = doc.getElementsByTagName("VisualParts").item(carId);
				fx.log("|| -> Loaded old parts into memory.");
				doc.getElementsByTagName("CustomCar").item(carId).replaceChild(Paints, oldPaints);
				doc.getElementsByTagName("CustomCar").item(carId).replaceChild(PerformanceParts, oldPerformanceParts);
				doc.getElementsByTagName("CustomCar").item(carId).replaceChild(SkillModParts, oldSkillModParts);
				doc.getElementsByTagName("CustomCar").item(carId).replaceChild(Vinyls, oldVinyls);
				doc.getElementsByTagName("CustomCar").item(carId).replaceChild(VisualParts, oldVisualParts);
				fx.log("|| -> New parts exchanged with old parts.");
				Node OwnedCar = doc.getElementsByTagName("OwnedCarTrans").item(carId);
				DOMImplementationLS lsImpl = (DOMImplementationLS)OwnedCar.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
				LSSerializer serializer = lsImpl.createLSSerializer();
				serializer.getDomConfig().setParameter("xml-declaration", false);
				String StringOwnedCar = serializer.writeToString(OwnedCar);
				fx.WriteTempCar(StringOwnedCar);
				fx.WriteXML(doc, "../nfsw/Engine.svc/personas/"+Functions.personaId+"/carslots.xml");
	
				fx.log("|| Commerce data was finalized. [carIndex = "+carId+"]");
			} else { 
				Functions.answerData = "";
				fx.log(" || !! -> You do not have enough balance for this type!"); 
			}
			//this.ChangeClass();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}			
	}
	
	public void sell(String data, int type) throws SAXException, IOException, ParserConfigurationException {
		// 0 -> AM, 1 -> Performance && Skills
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = docBuilder.parse("../nfsw/Engine.svc/personas/" + Functions.personaId + "/objects.xml");
		
		switch(type){
			case 0: {
				fx.log("|| Aftermarket part sell action detected.");
				
				int soldPartIndex = fx.CountInstances(fx.ReadText("../nfsw/Engine.svc/personas/" + Functions.personaId + "/objects.xml"), "<InventoryItemTrans>", "<EntitlementTag>"+data+"</EntitlementTag>")-1;
				
				fx.log("  || Initializing economy module.");
				NfswEconomy economy = new NfswEconomy(doc.getElementsByTagName("ResellPrice").item(soldPartIndex).getTextContent(), "0", true);
				fx.log("  || -> " + doc.getElementsByTagName("ResellPrice").item(soldPartIndex).getTextContent() + "IGC to be added to your balance.");
				economy.transCurrency(false);
				
				int remainingUses = Integer.parseInt(doc.getElementsByTagName("RemainingUseCount").item(soldPartIndex).getTextContent());
				fx.log("|| -> Remaining Uses: " + String.valueOf(remainingUses - 1));
				if (remainingUses - 1 <= 0 ) {
					Node partToSell = doc.getElementsByTagName("InventoryItemTrans").item(soldPartIndex);
					doc.getElementsByTagName("InventoryItems").item(0).removeChild(partToSell);
					int newPartAmount = Integer.parseInt(doc.getElementsByTagName("VisualPartsUsedSlotCount").item(0).getTextContent()) - 1;
					doc.getElementsByTagName("VisualPartsUsedSlotCount").item(0).setTextContent(String.valueOf(newPartAmount));
					fx.log("|| -> The part has been removed from your inventory.");
				} else {
					doc.getElementsByTagName("RemainingUseCount").item(soldPartIndex).setTextContent(String.valueOf(remainingUses - 1));
				}
				break;
			}
			case 1: {
				Document doc2 = docBuilder.parse(new InputSource(new StringReader(data)));
				
				NfswEconomy economy = new NfswEconomy();
				
				int Occur = fx.CountInstances(data, "<EntitlementId>", "<UpdatedCar>");
				fx.log("  || -> " + String.valueOf(Occur) + " part(s) to be sold.");
				
				for (int i = 0; i < Occur; i++) {
					String entitlementId = doc2.getElementsByTagName("EntitlementId").item(i).getTextContent();
					int soldPartIndex = fx.CountInstances(fx.ReadText("../nfsw/Engine.svc/personas/" + Functions.personaId + "/objects.xml"), "<InventoryItemTrans>", "<EntitlementTag>"+entitlementId+"</EntitlementTag>")-1;
					for (int j = 0; j < Integer.parseInt(doc2.getElementsByTagName("Quantity").item(i).getTextContent()); j++) {
						economy.AddPartToSell(doc.getElementsByTagName("ResellPrice").item(soldPartIndex).getTextContent());
					}
					
					int remainingUses = Integer.parseInt(doc.getElementsByTagName("RemainingUseCount").item(soldPartIndex).getTextContent());
					fx.log("    || -> Remaining uses of part " + String.valueOf(Occur) + ": " + String.valueOf(remainingUses - 1));
					if (remainingUses - 1 <= 0 ) {
						Node partToSell = doc.getElementsByTagName("InventoryItemTrans").item(soldPartIndex);
						switch (doc.getElementsByTagName("VirtualItemType").item(soldPartIndex).getTextContent()) {
							case "SKILLMODPART": {
								int newPartAmount = Integer.parseInt(doc.getElementsByTagName("SkillModPartsUsedSlotCount").item(0).getTextContent()) - 1;
								doc.getElementsByTagName("SkillModPartsUsedSlotCount").item(0).setTextContent(String.valueOf(newPartAmount));
								break;
							}
							case "VISUALPART": {
								int newPartAmount = Integer.parseInt(doc.getElementsByTagName("VisualPartsUsedSlotCount").item(0).getTextContent()) - 1;
								doc.getElementsByTagName("VisualPartsUsedSlotCount").item(0).setTextContent(String.valueOf(newPartAmount));
								break;
							}
							case "PERFORMANCEPART": {
								int newPartAmount = Integer.parseInt(doc.getElementsByTagName("PerformancePartsUsedSlotCount").item(0).getTextContent()) - 1;
								doc.getElementsByTagName("PerformancePartsUsedSlotCount").item(0).setTextContent(String.valueOf(newPartAmount));
								break;
							}
						}
						doc.getElementsByTagName("InventoryItems").item(0).removeChild(partToSell);
						fx.log("    || -> Part " + String.valueOf(Occur) + " has been removed from your inventory.");
					} else {
						doc.getElementsByTagName("RemainingUseCount").item(soldPartIndex).setTextContent(String.valueOf(remainingUses - 1));
					}
				}
				economy.SellParts();
				
				break;
			}
		}
		fx.WriteXML(doc, "../nfsw/Engine.svc/personas/" + Functions.personaId + "/objects.xml");
	}
	
	private String mapProductId(String productId) {
		if (productId.contains("SRV-PERF")) { 
			return "productsInCategory_NFSW_NA_EP_PERFORMANCEPARTS.xml";
		}
		else if (productId.contains("SRV-PBODY")) { 
			return "productsInCategory_NFSW_NA_EP_PAINTS_BODY_Category.xml";
		}
		else if (productId.contains("SRV-PWHEEL")) { 
			return "productsInCategory_NFSW_NA_EP_PAINTS_WHEEL_Category.xml";
		}
		else if (productId.contains("SRV-SKILL")) { 
			return "productsInCategory_NFSW_NA_EP_SKILLMODPARTS.xml";
		}
		else if (productId.contains("SRV-EPLICENSE")) { 
			return "productsInCategory_NFSW_NA_EP_VISUALPARTS_LICENSEPLATES.xml";
		}
		else if (productId.contains("SRV-EPNEON")) { 
			return "productsInCategory_NFSW_NA_EP_VISUALPARTS_NEONS.xml";
		}
		else if (productId.contains("SRV-EPWHEEL")) { 
			return "productsInCategory_NFSW_NA_EP_VISUALPARTS_WHEELS.xml";
		}
		else if (productId.contains("SRV-BODY")) { 
			return "productsInCategory_STORE_VANITY_BODYKIT.xml";
		}
		else if (productId.contains("SRV-HOOD")) { 
			return "productsInCategory_STORE_VANITY_HOOD.xml";
		}
		else if (productId.contains("SRV-LICENSE")) { 
			return "productsInCategory_STORE_VANITY_LICENSE_PLATE.xml";
		}
		else if (productId.contains("SRV-LOWKIT")) { 
			return "productsInCategory_STORE_VANITY_LOWERING_KIT.xml";
		}
		else if (productId.contains("SRV-NEON")) { 
			return "productsInCategory_STORE_VANITY_NEON.xml";
		}
		else if (productId.contains("SRV-SPOILER")) { 
			return "productsInCategory_STORE_VANITY_SPOILER.xml";
		}
		else if (productId.contains("SRV-WHEEL")) { 
			return "productsInCategory_STORE_VANITY_WHEEL.xml";
		}
		else if (productId.contains("SRV-WINDOW")) { 
			return "productsInCategory_STORE_VANITY_WINDOW.xml";
		}
		return null;
	}
	
	/*private void ChangeClass() {
		try {
			Integer carId = Integer.parseInt(this.functions.ReadCarIndex());

			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse("../nfsw/Engine.svc/personas/22418201/carslots.xml");
			
			Integer rating = Integer.parseInt(doc.getElementsByTagName("Rating").item(carId).getTextContent());

			if (rating > 0 && rating < 250) { doc.getElementsByTagName("CarClassHash").item(carId).setTextContent("872416321"); }
			else if (rating >= 250 && rating < 400) { doc.getElementsByTagName("CarClassHash").item(carId).setTextContent("415909161"); }
			else if (rating >= 400 && rating < 500) { doc.getElementsByTagName("CarClassHash").item(carId).setTextContent("1866825865"); }
			else if (rating >= 500 && rating < 600) { doc.getElementsByTagName("CarClassHash").item(carId).setTextContent("-406473455"); }
			else if (rating >= 600 && rating < 750) { doc.getElementsByTagName("CarClassHash").item(carId).setTextContent("-405837480"); }
			else if (rating >= 750 && rating < 1000) { doc.getElementsByTagName("CarClassHash").item(carId).setTextContent("-2142411446"); }
			
	        Transformer transformer = TransformerFactory.newInstance().newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("../nfsw/Engine.svc/personas/22418201/carslots.xml"));
			transformer.transform(source, result);
			
			Node OwnedCar = doc.getElementsByTagName("OwnedCarTrans").item(carId);
			DOMImplementationLS lsImpl = (DOMImplementationLS)OwnedCar.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
			LSSerializer serializer = lsImpl.createLSSerializer();
			serializer.getDomConfig().setParameter("xml-declaration", false);
			String StringOwnedCar = serializer.writeToString(OwnedCar);
			this.functions.WriteTempCar(StringOwnedCar);
		} catch (ParserConfigurationException pce) {
		pce.printStackTrace();
		} catch (TransformerException tfe) {
		tfe.printStackTrace();
		} catch (IOException ioe) {
		ioe.printStackTrace();
		} catch (SAXException sae) {
		sae.printStackTrace();
		}			
	}*/
}