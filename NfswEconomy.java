package br.com.hosts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class NfswEconomy {
	
	private Functions fx = new Functions();

	public int IGC = 0;
	public int Boost = 1;

	public static int amount;
	public static int type;
	
	private int[] totalPrice = new int[] {0, 0};
	
	public boolean transCurrency(boolean remove) {
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse("../nfsw/Engine.svc/User/GetPermanentSession.xml");
			int sessionId = Integer.parseInt(doc.getElementsByTagName("defaultPersonaIdx").item(0).getTextContent());
			int newAmount = 0;
			if (type == IGC) {
				Document doc2 = docBuilder.parse("../nfsw/Engine.svc/DriverPersona/GetPersonaInfo_"+Functions.personaId+".xml");
				newAmount = Integer.parseInt(doc2.getElementsByTagName("Cash").item(0).getTextContent());
				if (remove) { newAmount = newAmount - amount; if (newAmount < 0) return false; }
				else { newAmount = newAmount + amount; if (newAmount > 999999999) { newAmount = 999999999; } }
				doc.getElementsByTagName("Cash").item(sessionId).setTextContent(String.valueOf(newAmount));
				doc2.getElementsByTagName("Cash").item(0).setTextContent(String.valueOf(newAmount));
				fx.WriteXML(doc2, "../nfsw/Engine.svc/DriverPersona/GetPersonaInfo_"+Functions.personaId+".xml");
			}
			else if (type == Boost) {
				newAmount = Integer.parseInt(doc.getElementsByTagName("Boost").item(sessionId).getTextContent());
				if (remove) { newAmount = newAmount - amount; if (newAmount < 0) return false; }
				else { newAmount = newAmount + amount; if (newAmount > 999999999) { newAmount = 999999999; } }
				for(int i = 0; i < 3; i++) { doc.getElementsByTagName("Boost").item(i).setTextContent(String.valueOf(newAmount)); }
			}
			amount = newAmount; //for commerce
			fx.WriteXML(doc, "../nfsw/Engine.svc/User/GetPermanentSession.xml");
			if (NfswSrv.modifiedTarget == "baskets") Functions.answerData = "<CommerceResultTrans xmlns=\"http://schemas.datacontract.org/2004/07/Victory.DataLayer.Serialization\" xmlns:i=\"http://../.w3.org/2001/XMLSchema-instance\"><CommerceItems /><InvalidBasket i:nil=\"true\" /><InventoryItems><InventoryItemTrans><EntitlementTag>CYKABLYATMIDFEED</EntitlementTag><ExpirationDate i:nil=\"true\" /><Hash>831687504</Hash><InventoryId>2897169042</InventoryId><ProductId>ALLRAPORTBLOODCYKA</ProductId><RemainingUseCount>0</RemainingUseCount><ResellPrice>0.02578</ResellPrice><Status>ACTIVE</Status><StringHash>0x31928b50</StringHash><VirtualItemType>presetcar</VirtualItemType></InventoryItemTrans></InventoryItems><PurchasedCars><OwnedCarTrans><CustomCar><BaseCar>1008844988</BaseCar><CarClassHash>415909161</CarClassHash><Id>120</Id><IsPreset>true</IsPreset><Level>0</Level><Name>offlineserverbasket</Name><Paints><CustomPaintTrans><Group>47885063</Group><Hue>496032397</Hue><Sat>231</Sat><Slot>0</Slot><Var>181</Var></CustomPaintTrans><CustomPaintTrans><Group>47885063</Group><Hue>496032397</Hue><Sat>231</Sat><Slot>3</Slot><Var>181</Var></CustomPaintTrans><CustomPaintTrans><Group>47885063</Group><Hue>496032397</Hue><Sat>231</Sat><Slot>4</Slot><Var>181</Var></CustomPaintTrans><CustomPaintTrans><Group>47885063</Group><Hue>496032397</Hue><Sat>231</Sat><Slot>5</Slot><Var>181</Var></CustomPaintTrans><CustomPaintTrans><Group>47885063</Group><Hue>496032397</Hue><Sat>231</Sat><Slot>6</Slot><Var>181</Var></CustomPaintTrans><CustomPaintTrans><Group>47885063</Group><Hue>496032397</Hue><Sat>231</Sat><Slot>7</Slot><Var>181</Var></CustomPaintTrans></Paints><PerformanceParts><PerformancePartTrans><PerformancePartAttribHash>-1423940189</PerformancePartAttribHash></PerformancePartTrans><PerformancePartTrans><PerformancePartAttribHash>-1080430637</PerformancePartAttribHash></PerformancePartTrans><PerformancePartTrans><PerformancePartAttribHash>364511990</PerformancePartAttribHash></PerformancePartTrans><PerformancePartTrans><PerformancePartAttribHash>1056323105</PerformancePartAttribHash></PerformancePartTrans><PerformancePartTrans><PerformancePartAttribHash>1285357754</PerformancePartAttribHash></PerformancePartTrans><PerformancePartTrans><PerformancePartAttribHash>1572779790</PerformancePartAttribHash></PerformancePartTrans></PerformanceParts><PhysicsProfileHash>-1550817422</PhysicsProfileHash><Rating>308</Rating><ResalePrice>0</ResalePrice><RideHeightDrop>0.75</RideHeightDrop><SkillModParts /><SkillModSlotCount>5</SkillModSlotCount><Version>0</Version><Vinyls><CustomVinylTrans><Hash>871751947</Hash><Hue1>-799662386</Hue1><Hue2>0</Hue2><Hue3>0</Hue3><Hue4>0</Hue4><Layer>0</Layer><Mir>false</Mir><Rot>0</Rot><Sat1>0</Sat1><Sat2>0</Sat2><Sat3>0</Sat3><Sat4>0</Sat4><ScaleX>0</ScaleX><ScaleY>0</ScaleY><Shear>0</Shear><TranX>0</TranX><TranY>0</TranY><Var1>255</Var1><Var2>0</Var2><Var3>0</Var3><Var4>0</Var4></CustomVinylTrans><CustomVinylTrans><Hash>481719807</Hash><Hue1>-799662386</Hue1><Hue2>0</Hue2><Hue3>0</Hue3><Hue4>0</Hue4><Layer>1</Layer><Mir>false</Mir><Rot>0</Rot><Sat1>0</Sat1><Sat2>0</Sat2><Sat3>0</Sat3><Sat4>0</Sat4><ScaleX>0</ScaleX><ScaleY>0</ScaleY><Shear>0</Shear><TranX>0</TranX><TranY>0</TranY><Var1>255</Var1><Var2>0</Var2><Var3>0</Var3><Var4>0</Var4></CustomVinylTrans><CustomVinylTrans><Hash>-935349776</Hash><Hue1>-799662383</Hue1><Hue2>0</Hue2><Hue3>0</Hue3><Hue4>0</Hue4><Layer>2</Layer><Mir>false</Mir><Rot>119</Rot><Sat1>231</Sat1><Sat2>0</Sat2><Sat3>0</Sat3><Sat4>0</Sat4><ScaleX>655</ScaleX><ScaleY>655</ScaleY><Shear>0</Shear><TranX>2</TranX><TranY>224</TranY><Var1>179</Var1><Var2>0</Var2><Var3>0</Var3><Var4>0</Var4></CustomVinylTrans></Vinyls><VisualParts><VisualPartTrans><PartHash>-1674761484</PartHash><SlotHash>-2126743923</SlotHash></VisualPartTrans><VisualPartTrans><PartHash>-583397510</PartHash><SlotHash>-1515028479</SlotHash></VisualPartTrans><VisualPartTrans><PartHash>1670368247</PartHash><SlotHash>-966088147</SlotHash></VisualPartTrans><VisualPartTrans><PartHash>1887592399</PartHash><SlotHash>-1505530948</SlotHash></VisualPartTrans></VisualParts></CustomCar><Durability>100</Durability><ExpirationDate i:nil=\"true\" /><Heat>1</Heat><Id>72861440</Id><OwnershipType>PresetCar</OwnershipType></OwnedCarTrans></PurchasedCars><Status>Success</Status><Wallets><WalletTrans><Balance>" + String.valueOf(newAmount) + "</Balance><Currency>" + (type == IGC ? "CASH":"BOOST") + "</Currency></WalletTrans></Wallets></CommerceResultTrans>";
			fx.log("  || -> Your new" + (type == IGC ? " IGC ":" Boost ") + "balance is " + String.valueOf(newAmount) + ".");
			return true;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public NfswEconomy() {
		amount = 0; type = 0; totalPrice = new int[] {0,0};
		fx.log("  || -> Economy Module running.");
	}

	public NfswEconomy(String catalogName, String productId, Boolean type2) {
		try {
			if (catalogName == null || productId == null) { fx.log("  || -> Support for this product has not been added, yet."); return; }
			amount = 0; type = 0; totalPrice = new int[] {0,0};
			
		    fx.log("  || -> Economy Module running.");
			if (type2) {
				amount = Double.valueOf(catalogName).intValue();
				type = Integer.parseInt(productId);
			} else {
				int catID = fx.CountInstances(new String(Files.readAllBytes(Paths.get("../nfsw/Engine.svc/catalog/" + catalogName)), StandardCharsets.UTF_8), "<ProductTrans>", "<ProductId>"+productId+"</ProductId>")-1;
				DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document catalog = docBuilder.parse("../nfsw/Engine.svc/catalog/" + catalogName);

				amount = Double.valueOf(catalog.getElementsByTagName("Price").item(catID).getTextContent()).intValue();
				type = catalog.getElementsByTagName("Currency").item(catID).getTextContent().equals("CASH") ? IGC : Boost;
				String productTitle = catalog.getElementsByTagName("ProductTitle").item(catID).getTextContent().isEmpty() ? "'Unknown Item'":catalog.getElementsByTagName("ProductTitle").item(catID).getTextContent();
				fx.log("  || -> Purchasing package " + fx.toTitleCase(productTitle) + " for " + String.valueOf(amount) + (type == IGC ? " IGC":" Boost") + ".");
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void AddCommerce(String catalogName, String productId) {
		try {
			if (catalogName == null || productId == null) { fx.log("    || Support for this product has not been added, yet."); return; }
			int catID = fx.CountInstances(new String(Files.readAllBytes(Paths.get("../nfsw/Engine.svc/catalog/" + catalogName)), StandardCharsets.UTF_8), "<ProductTrans>", "<ProductId>"+productId+"</ProductId>")-1;
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document catalog = docBuilder.parse("../nfsw/Engine.svc/catalog/" + catalogName);

			amount = Double.valueOf(catalog.getElementsByTagName("Price").item(catID).getTextContent()).intValue();
			type = catalog.getElementsByTagName("Currency").item(catID).getTextContent().equals("CASH") ? IGC : Boost;
			totalPrice[type == IGC ? 0 : 1] += Double.valueOf(catalog.getElementsByTagName("Price").item(catID).getTextContent()).intValue();
			String productTitle = catalog.getElementsByTagName("ProductTitle").item(catID).getTextContent().isEmpty() ? "Unknown Item":catalog.getElementsByTagName("ProductTitle").item(catID).getTextContent();
			fx.log("    || Added package " + fx.toTitleCase(productTitle) + " for " + String.valueOf(amount) + (type == IGC ? " IGC":" Boost") + " to the shopping list.");
			fx.log("    || -> Total Price: " + String.valueOf(totalPrice[0]) + " IGC and " + String.valueOf(totalPrice[1]) + " Boost");
		} catch (IOException | ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean DoCommerce() { if (totalPrice[0] > 0) { fx.log("  || -> Trying for " + String.valueOf(totalPrice[0]) + " IGC."); type = IGC; amount = totalPrice[0]; if (!transCurrency(true)) return false; else fx.log("  || IGC transaction was successful."); }
							   if (totalPrice[1] > 0) { fx.log("  || -> Trying for " + String.valueOf(totalPrice[1]) + " Boost."); type = Boost; amount = totalPrice[1]; if (!transCurrency(true)) return false; else fx.log(" || Boost transaction was successful."); } 
							   return true;
							 }
	
	public void AddPartToSell(String price) {
		totalPrice[0] += (int) Math.round(Double.valueOf(price));
		fx.log("    || -> Added one part for " + String.valueOf((int) Math.round(Double.valueOf(price))) + "IGC.");
	}
	
	public void SellParts() {
		if (totalPrice[0] != 0) {
			fx.log("  || -> " + String.valueOf(totalPrice[0]) + "IGC to be added to your balance.");
			type = IGC; amount = totalPrice[0]; transCurrency(false);
		}
	}
	
}