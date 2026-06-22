import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;

// ==================== DESIGN SYSTEM ====================
class DS {
    // Dark command-center palette
    static final Color BG_DEEP    = new Color(0x0A0F1E);  // deepest navy
    static final Color BG_BASE    = new Color(0x0D1527);  // base panel
    static final Color BG_PANEL   = new Color(0x111D35);  // card bg
    static final Color BG_HOVER   = new Color(0x16274A);  // hover state
    static final Color BG_RAISED  = new Color(0x1A2F52);  // elevated surface

    // Accent hierarchy
    static final Color ACCENT_AMBER  = new Color(0xF5A623); // primary alert/emergency
    static final Color ACCENT_CYAN   = new Color(0x00C8FF); // primary interactive
    static final Color ACCENT_TEAL   = new Color(0x00E5CC); // secondary
    static final Color ACCENT_VIOLET = new Color(0x7B61FF); // highlight

    // Semantic colors
    static final Color SAFE    = new Color(0x00D46A);
    static final Color WARN    = new Color(0xFFB020);
    static final Color DANGER  = new Color(0xFF4136);
    static final Color CRITICAL= new Color(0xFF0055);
    static final Color DISPATCH= new Color(0xF5A623);

    // Text colors
    static final Color TEXT_PRIMARY   = new Color(0xE8EDFB);
    static final Color TEXT_SECONDARY = new Color(0x8090B0);
    static final Color TEXT_MUTED     = new Color(0x4A5878);
    static final Color TEXT_ACCENT    = ACCENT_CYAN;

    // Borders
    static final Color BORDER_SUBTLE  = new Color(0x1E3058);
    static final Color BORDER_ACTIVE  = new Color(0x2A4A7F);
    static final Color BORDER_ACCENT  = new Color(0x00C8FF, true);

    // Fonts — system stack with bold data styling
    static final Font FONT_DISPLAY  = new Font("Segoe UI", Font.BOLD, 22);
    static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 14);
    static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN, 11);
    static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD, 11);
    static final Font FONT_HUGE     = new Font("Segoe UI", Font.BOLD, 32);
    static final Font FONT_BADGE    = new Font("Segoe UI", Font.BOLD, 10);

    static void applyGlobalDefaults() {
        UIManager.put("Panel.background", BG_BASE);
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Button.background", BG_RAISED);
        UIManager.put("Button.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.background", BG_RAISED);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", ACCENT_CYAN);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_ACTIVE, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        UIManager.put("ComboBox.background", BG_RAISED);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("List.background", BG_PANEL);
        UIManager.put("List.foreground", TEXT_PRIMARY);
        UIManager.put("List.selectionBackground", BG_HOVER);
        UIManager.put("List.selectionForeground", ACCENT_CYAN);
        UIManager.put("ScrollPane.background", BG_BASE);
        UIManager.put("ScrollBar.background", BG_BASE);
        UIManager.put("ScrollBar.thumb", BORDER_ACTIVE);
        UIManager.put("TabbedPane.background", BG_BASE);
        UIManager.put("TabbedPane.foreground", TEXT_SECONDARY);
        UIManager.put("TabbedPane.selected", BG_PANEL);
        UIManager.put("TabbedPane.selectedForeground", ACCENT_CYAN);
        UIManager.put("TitledBorder.titleColor", TEXT_SECONDARY);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
    }
}

// ==================== ENUMS ====================
enum Severity {
    LOW(1, "●", "Low", DS.SAFE),
    MEDIUM(2, "●", "Medium", DS.WARN),
    HIGH(3, "▲", "High", DS.ACCENT_AMBER),
    CRITICAL(4, "▲", "Critical", DS.DANGER),
    DISASTER(5, "◆", "Disaster", DS.CRITICAL);

    final int level; final String icon; final String displayName; final Color color;
    Severity(int l, String i, String d, Color c) { level=l; icon=i; displayName=d; color=c; }
    public int getLevel() { return level; }
    public String getIcon() { return icon; }
    public String getDisplayName() { return displayName; }
    public Color getColor() { return color; }
}

enum VehicleType {
    AMBULANCE("🚑", "Ambulance", true, DS.DANGER),
    FIRE_TRUCK("🚒", "Fire Truck", true, DS.ACCENT_AMBER),
    POLICE("🚓", "Police Car", true, DS.ACCENT_VIOLET),
    BUS("🚌", "City Bus", false, DS.ACCENT_TEAL),
    CAR("🚗", "Car", false, DS.TEXT_SECONDARY),
    TAXI("🚕", "Taxi", false, DS.WARN);

    final String icon; final String displayName; final boolean emergency; final Color color;
    VehicleType(String i, String d, boolean e, Color c) { icon=i; displayName=d; emergency=e; color=c; }
    public String getIcon() { return icon; }
    public String getDisplayName() { return displayName; }
    public boolean isEmergency() { return emergency; }
    public Color getColor() { return color; }
}

enum Direction { NORTH, SOUTH, EAST, WEST }

// ==================== MODEL CLASSES ====================
class MapMarker {
    final String id, intersectionId, label, icon;
    final Color color;
    final long timestamp;
    MapMarker(String id, String iId, String label, Color color, String icon) {
        this.id=id; this.intersectionId=iId; this.label=label; this.color=color; this.icon=icon;
        this.timestamp=System.currentTimeMillis();
    }
}

class Address {
    final String streetName, intersectionId, landmark;
    final double lat, lon;
    Address(String sn, String iId, String lm, double la, double lo) {
        streetName=sn; intersectionId=iId; landmark=lm; lat=la; lon=lo;
    }
    public String getFullAddress() { return String.format("%s (%s) — %s", streetName, intersectionId, landmark); }
    public String getShortAddress() { return String.format("%s [%s]", streetName, intersectionId); }
}

class Vehicle {
    final String id; final VehicleType type; double lat, lon;
    List<String> route = new ArrayList<>();
    final boolean emergency;
    private long lastUpdate; private String currentStatus="Available", destination="";
    Vehicle(String id, VehicleType type, double lat, double lon) {
        this.id=id; this.type=type; this.lat=lat; this.lon=lon; this.emergency=type.isEmergency();
        this.lastUpdate=System.currentTimeMillis();
    }
    public void updatePosition(double la, double lo) { lat=la; lon=lo; lastUpdate=System.currentTimeMillis(); }
    public long getLastUpdate() { return lastUpdate; }
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String s) { currentStatus=s; }
    public String getDestination() { return destination; }
    public void setDestination(String d) { destination=d; }
}

class Incident {
    final Severity severity; final String location, description;
    final double timestamp; boolean resolved;
    final String incidentId;
    private static int counter=0;
    Incident(Severity sev, String loc, String desc) {
        severity=sev; location=loc; description=desc;
        timestamp=System.currentTimeMillis()/1000.0; resolved=false;
        incidentId="INC"+(++counter);
    }
    public String getFormattedTime() {
        return LocalDateTime.ofEpochSecond((long)timestamp,0,java.time.ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}

class TrafficSignal {
    private boolean emergencyActive=false; private String currentPhase="GREEN";
    public void emergencyOverride(Direction d) { emergencyActive=true; currentPhase="EMERGENCY"; }
    public boolean isEmergencyActive() { return emergencyActive; }
    public void clearEmergency() { emergencyActive=false; currentPhase="GREEN"; }
    public String getCurrentPhase() { return currentPhase; }
}

class Intersection {
    final String id; final double lat, lon;
    final Map<String,Double> connections=new HashMap<>();
    private int congestionLevel=0; private boolean isBlocked=false; private String blockReason="";
    Intersection(String id, double lat, double lon) { this.id=id; this.lat=lat; this.lon=lon; }
    public void updateCongestion(int level) { congestionLevel=Math.max(0,Math.min(10,level)); }
    public int getCongestionLevel() { return congestionLevel; }
    public void setBlocked(boolean b, String r) { isBlocked=b; blockReason=r; }
    public boolean isBlocked() { return isBlocked; }
    public String getBlockReason() { return blockReason; }
}

class RouteInfo {
    final List<String> path; final double distance; final int routeNumber;
    RouteInfo(List<String> p, double d, int n) { path=p; distance=d; routeNumber=n; }
    public String getDisplayString() {
        return String.format("Route %d  ·  %s  ·  %.2f km", routeNumber, String.join(" → ", path), distance);
    }
}

class DijkstraResult {
    final double distance; final List<String> path;
    DijkstraResult(double d, List<String> p) { distance=d; path=p; }
    public boolean isPathFound() { return distance<Double.MAX_VALUE && !path.isEmpty(); }
}

// ==================== CITY MANAGER ====================
class CityManager {
    private final Map<String,Intersection> graph=new HashMap<>();
    private final Map<String,Vehicle> vehicles=new HashMap<>();
    private final List<Incident> incidents=new ArrayList<>();
    private final Map<String,TrafficSignal> trafficSignals=new HashMap<>();
    private List<String> activeEmergencyRoute=null;
    private String dispatchedVehicleId=null;
    private final Random random=new Random();
    private final List<String> systemLogs=new ArrayList<>();
    private final Set<String> blockedRoads=new HashSet<>();
    private final Map<String,Address> addressBook=new HashMap<>();
    private final Map<String,MapMarker> markers=new HashMap<>();
    private int markerCounter=0;

    CityManager() { initCity(); initAddresses(); }

    private void initCity() {
        for(int i=0;i<4;i++) for(int j=0;j<4;j++) {
            String id=String.format("I%d%d",i,j);
            graph.put(id,new Intersection(id,i,j));
            trafficSignals.put(id,new TrafficSignal());
        }
        for(int i=0;i<4;i++) for(int j=0;j<4;j++) {
            String id=String.format("I%d%d",i,j);
            if(j<3){String r=String.format("I%d%d",i,j+1);graph.get(id).connections.put(r,1.0);graph.get(r).connections.put(id,1.0);}
            if(i<3){String d=String.format("I%d%d",i+1,j);graph.get(id).connections.put(d,1.0);graph.get(d).connections.put(id,1.0);}
        }
        vehicles.put("AMB001",new Vehicle("AMB001",VehicleType.AMBULANCE,0,0));
        vehicles.put("AMB002",new Vehicle("AMB002",VehicleType.AMBULANCE,3,3));
        vehicles.put("AMB003",new Vehicle("AMB003",VehicleType.AMBULANCE,1,2));
        vehicles.put("POL001",new Vehicle("POL001",VehicleType.POLICE,1,2));
        vehicles.put("POL002",new Vehicle("POL002",VehicleType.POLICE,2,1));
        vehicles.put("POL003",new Vehicle("POL003",VehicleType.POLICE,3,0));
        vehicles.put("FDN001",new Vehicle("FDN001",VehicleType.FIRE_TRUCK,2,1));
        vehicles.put("FDN002",new Vehicle("FDN002",VehicleType.FIRE_TRUCK,0,3));
        vehicles.put("CAR001",new Vehicle("CAR001",VehicleType.CAR,1,1));
        vehicles.put("CAR002",new Vehicle("CAR002",VehicleType.CAR,2,2));
        vehicles.put("BUS001",new Vehicle("BUS001",VehicleType.BUS,0,2));
        vehicles.put("TAX001",new Vehicle("TAX001",VehicleType.TAXI,3,0));
    }

    private void initAddresses() {
        addressBook.put("I00",new Address("Main Street North","I00","City Hall",0,0));
        addressBook.put("I01",new Address("Park Avenue","I01","Central Park",0,1));
        addressBook.put("I02",new Address("Broadway","I02","Shopping Mall",0,2));
        addressBook.put("I03",new Address("Ocean Drive","I03","Beach",0,3));
        addressBook.put("I10",new Address("King Street","I10","Hospital",1,0));
        addressBook.put("I11",new Address("Queen Street","I11","Town Square",1,1));
        addressBook.put("I12",new Address("Market Street","I12","Market",1,2));
        addressBook.put("I13",new Address("Church Street","I13","Cathedral",1,3));
        addressBook.put("I20",new Address("River Road","I20","Bridge",2,0));
        addressBook.put("I21",new Address("Hill Street","I21","View Point",2,1));
        addressBook.put("I22",new Address("Valley Road","I22","Business District",2,2));
        addressBook.put("I23",new Address("Forest Lane","I23","Forest Park",2,3));
        addressBook.put("I30",new Address("Lake Shore Drive","I30","Lake",3,0));
        addressBook.put("I31",new Address("Mountain View","I31","Scenic Point",3,1));
        addressBook.put("I32",new Address("Sunset Boulevard","I32","Sunset Point",3,2));
        addressBook.put("I33",new Address("Victory Road","I33","Stadium",3,3));
    }

    public Map<String,Vehicle> getVehicles() { return vehicles; }
    public Map<String,TrafficSignal> getTrafficSignals() { return trafficSignals; }
    public List<Incident> getAllIncidents() { return incidents; }
    public List<String> getActiveEmergencyRoute() { return activeEmergencyRoute; }
    public void setActiveEmergencyRoute(List<String> r) { activeEmergencyRoute=r; }
    public Set<String> getBlockedRoads() { return blockedRoads; }
    public String getDispatchedVehicleId() { return dispatchedVehicleId; }
    public Map<String,Address> getAddressBook() { return addressBook; }
    public Map<String,MapMarker> getMarkers() { return markers; }
    public List<Address> getAllAddresses() { return new ArrayList<>(addressBook.values()); }
    public Map<String,Intersection> getGraph() { return graph; }

    public void addMarker(String iId,String label,Color color,String icon) {
        String mid="M"+(++markerCounter);
        markers.put(mid,new MapMarker(mid,iId,label,color,icon));
        addLog(String.format("MARKER  Added '%s' at %s",label,iId));
    }
    public void removeMarker(String mid) {
        if(markers.containsKey(mid)){MapMarker m=markers.get(mid);markers.remove(mid);addLog("MARKER  Removed '"+m.label+"'");}
    }
    public void clearAllMarkers() { markers.clear(); addLog("MARKER  All markers cleared"); }

    public Incident getIncidentAtIntersection(String iId) {
        return incidents.stream().filter(i->!i.resolved&&i.location.equals(iId)).findFirst().orElse(null);
    }

    public List<Vehicle> getAvailableEmergencyVehicles() {
        List<Vehicle> ev=new ArrayList<>();
        for(Vehicle v:vehicles.values()) if(v.emergency&&v.getCurrentStatus().equals("Available")) ev.add(v);
        return ev;
    }

    public String searchAddress(String query) {
        query=query.toLowerCase().trim();
        if(addressBook.containsKey(query.toUpperCase())) return query.toUpperCase();
        for(Map.Entry<String,Address> e:addressBook.entrySet()) {
            Address a=e.getValue();
            if(a.streetName.toLowerCase().contains(query)||a.landmark.toLowerCase().contains(query)) return e.getKey();
        }
        return null;
    }

    public void blockIntersection(String id, String reason) {
        if(graph.containsKey(id)){graph.get(id).setBlocked(true,reason);blockedRoads.add(id);addLog("BLOCK   "+id+" BLOCKED: "+reason);}
    }
    public void unblockIntersection(String id) {
        if(graph.containsKey(id)){graph.get(id).setBlocked(false,"");blockedRoads.remove(id);addLog("BLOCK   "+id+" unblocked");}
    }

    public List<RouteInfo> findAllAlternateRoutes(String start, String end, int maxRoutes) {
        List<RouteInfo> paths=new ArrayList<>();
        PriorityQueue<PathNode> potential=new PriorityQueue<>(Comparator.comparingDouble(p->p.distance));
        DijkstraResult first=findShortestPath(start,end);
        if(!first.isPathFound()) return paths;
        paths.add(new RouteInfo(first.path,first.distance,1));
        for(int k=1;k<maxRoutes;k++) {
            RouteInfo prev=paths.get(k-1);
            for(int si=0;si<prev.path.size()-1;si++) {
                List<String> root=new ArrayList<>(prev.path.subList(0,si+1));
                Set<String> rmEdges=new HashSet<>(), rmNodes=new HashSet<>();
                for(RouteInfo p:paths) if(p.path.size()>si&&p.path.subList(0,si+1).equals(root))
                    rmEdges.add(p.path.get(si)+"->"+p.path.get(si+1));
                for(int i=0;i<si;i++) rmNodes.add(prev.path.get(i));
                DijkstraResult spur=findShortestPathAvoiding(root.get(root.size()-1),end,rmEdges,rmNodes);
                if(spur.isPathFound()) {
                    List<String> tp=new ArrayList<>(root);
                    tp.addAll(spur.path.subList(1,spur.path.size()));
                    potential.offer(new PathNode(tp,calculatePathDist(tp)));
                }
            }
            if(potential.isEmpty()) break;
            PathNode next=potential.poll();
            paths.add(new RouteInfo(next.path,next.distance,paths.size()+1));
        }
        return paths;
    }

    public DijkstraResult findShortestPath(String s, String e) {
        return findShortestPathAvoiding(s,e,new HashSet<>(),new HashSet<>());
    }

    private DijkstraResult findShortestPathAvoiding(String s, String e, Set<String> avoidE, Set<String> avoidN) {
        if(!graph.containsKey(s)||!graph.containsKey(e)) return new DijkstraResult(Double.MAX_VALUE,new ArrayList<>());
        if(graph.get(s).isBlocked()||graph.get(e).isBlocked()) return new DijkstraResult(Double.MAX_VALUE,new ArrayList<>());
        Map<String,Double> dist=new HashMap<>(); Map<String,String> prev=new HashMap<>();
        PriorityQueue<NodeDist> pq=new PriorityQueue<>(Comparator.comparingDouble(n->n.dist));
        for(String n:graph.keySet()) dist.put(n,Double.MAX_VALUE);
        dist.put(s,0.0); pq.offer(new NodeDist(s,0.0));
        while(!pq.isEmpty()) {
            NodeDist cur=pq.poll();
            if(cur.id.equals(e)) break;
            if((graph.get(cur.id).isBlocked()&&!cur.id.equals(s))||avoidN.contains(cur.id)) continue;
            for(Map.Entry<String,Double> nb:graph.get(cur.id).connections.entrySet()) {
                if(avoidE.contains(cur.id+"->"+nb.getKey())) continue;
                if(graph.get(nb.getKey()).isBlocked()||avoidN.contains(nb.getKey())) continue;
                double cong=graph.get(nb.getKey()).getCongestionLevel()/10.0;
                double nd=cur.dist+nb.getValue()*(1+cong);
                if(nd<dist.get(nb.getKey())) { dist.put(nb.getKey(),nd); prev.put(nb.getKey(),cur.id); pq.offer(new NodeDist(nb.getKey(),nd)); }
            }
        }
        List<String> path=new ArrayList<>(); String cur=e;
        while(cur!=null){path.add(0,cur);cur=prev.get(cur);}
        if(path.size()==1||!path.get(0).equals(s)||!path.get(path.size()-1).equals(e))
            return new DijkstraResult(Double.MAX_VALUE,new ArrayList<>());
        return new DijkstraResult(dist.get(e),path);
    }

    private double calculatePathDist(List<String> path) {
        double d=0;
        for(int i=0;i<path.size()-1;i++) {
            String f=path.get(i),t=path.get(i+1);
            if(graph.containsKey(f)&&graph.get(f).connections.containsKey(t)) d+=graph.get(f).connections.get(t);
        }
        return d;
    }

    private static class PathNode { List<String> path; double distance; PathNode(List<String> p,double d){path=p;distance=d;} }
    private static class NodeDist { final String id; final double dist; NodeDist(String i,double d){id=i;dist=d;} }

    public Incident reportIncident(String loc, Severity sev, String desc) {
        Incident inc=new Incident(sev,loc,desc);
        incidents.add(inc);
        incidents.sort((a,b)->Integer.compare(b.severity.getLevel(),a.severity.getLevel()));
        if(sev.getLevel()>=Severity.HIGH.getLevel()) blockIntersection(loc,"Incident: "+desc);
        addLog("INCIDENT  "+sev.getDisplayName()+" at "+loc+": "+desc);
        return inc;
    }

    public boolean dispatchSpecificVehicle(String vehicleId, String loc) {
        if(!vehicles.containsKey(vehicleId)){addLog("DISPATCH  ERROR: "+vehicleId+" not found");return false;}
        Vehicle v=vehicles.get(vehicleId);
        if(!v.emergency||!v.getCurrentStatus().equals("Available")){addLog("DISPATCH  ERROR: "+vehicleId+" unavailable");return false;}
        String start=getNearestIntersection(v);
        DijkstraResult res=findShortestPath(start,loc);
        if(res.isPathFound()) {
            activeEmergencyRoute=res.path; dispatchedVehicleId=vehicleId;
            v.setCurrentStatus("En Route"); v.setDestination(loc);
            for(String inter:res.path){TrafficSignal ts=trafficSignals.get(inter);if(ts!=null) ts.emergencyOverride(Direction.EAST);}
            addLog(String.format("DISPATCH  %s %s → %s  (%.2f km)",v.type.getIcon(),vehicleId,loc,res.distance));
            return true;
        }
        addLog("DISPATCH  ERROR: no route from "+start+" to "+loc);
        return false;
    }

    public Vehicle dispatchClosestAmbulance(String loc) {
        if(loc.length()<3) return null;
        int li=Integer.parseInt(loc.substring(1,2)), lj=Integer.parseInt(loc.substring(2,3));
        return vehicles.values().stream()
                .filter(v->v.emergency&&v.getCurrentStatus().equals("Available"))
                .min(Comparator.comparingDouble(v->Math.abs(v.lat-li)+Math.abs(v.lon-lj))).orElse(null);
    }

    public String getNearestIntersection(Vehicle v) {
        int x=(int)Math.round(Math.max(0,Math.min(3,v.lat))), y=(int)Math.round(Math.max(0,Math.min(3,v.lon)));
        return String.format("I%d%d",x,y);
    }

    public void simulateTraffic() {
        for(Vehicle v:vehicles.values()) if(random.nextDouble()>0.7) {
            v.updatePosition(Math.max(0,Math.min(3,v.lat+(random.nextDouble()-0.5)*0.3)),
                    Math.max(0,Math.min(3,v.lon+(random.nextDouble()-0.5)*0.3)));
        }
        for(Intersection inter:graph.values()) {
            int nb=(int)vehicles.values().stream().filter(v->Math.abs(v.lat-inter.lat)<0.5&&Math.abs(v.lon-inter.lon)<0.5).count();
            inter.updateCongestion(nb*2);
        }
        addLog("TRAFFIC   Simulation tick — vehicles repositioned");
    }

    public void resolveIncident(String loc) {
        for(Incident inc:incidents) if(inc.location.equals(loc)&&!inc.resolved) {
            inc.resolved=true; unblockIntersection(loc);
            addLog("RESOLVE   Incident at "+loc+" resolved"); return;
        }
        addLog("RESOLVE   No active incident at "+loc);
    }

    public void completeDispatch() {
        if(dispatchedVehicleId!=null&&vehicles.containsKey(dispatchedVehicleId)) {
            Vehicle v=vehicles.get(dispatchedVehicleId);
            v.setCurrentStatus("Available"); v.setDestination("");
            addLog("COMPLETE  "+v.type.getIcon()+" "+dispatchedVehicleId+" mission complete");
            dispatchedVehicleId=null; activeEmergencyRoute=null;
            for(TrafficSignal ts:trafficSignals.values()) ts.clearEmergency();
        }
    }

    public void clearResolvedIncidents() {
        long n=incidents.stream().filter(i->i.resolved).count();
        incidents.removeIf(i->i.resolved);
        addLog("CLEANUP   "+n+" resolved incidents removed");
    }

    public Map<String,Object> getSystemStatus() {
        Map<String,Object> s=new HashMap<>();
        s.put("active_vehicles",vehicles.size());
        s.put("emergency_vehicles",vehicles.values().stream().filter(v->v.emergency).count());
        s.put("available_emergency",vehicles.values().stream().filter(v->v.emergency&&v.getCurrentStatus().equals("Available")).count());
        s.put("enroute_vehicles",vehicles.values().stream().filter(v->v.getCurrentStatus().equals("En Route")).count());
        s.put("active_incidents",incidents.stream().filter(i->!i.resolved).count());
        s.put("traffic_signals",trafficSignals.size());
        s.put("avg_congestion",graph.values().stream().mapToInt(Intersection::getCongestionLevel).average().orElse(0));
        s.put("blocked_roads",blockedRoads.size());
        s.put("markers_count",markers.size());
        s.put("timestamp",LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return s;
    }

    private void addLog(String msg) {
        String ts=LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        systemLogs.add(0,"["+ts+"] "+msg);
        if(systemLogs.size()>200) systemLogs.remove(systemLogs.size()-1);
    }
    public List<String> getSystemLogs() { return new ArrayList<>(systemLogs); }
}

// ==================== CUSTOM PAINT HELPERS ====================
class PaintHelper {
    static void paintDarkCard(Graphics2D g2, int x, int y, int w, int h, Color bg, Color borderColor, int arc) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x, y, w, h, arc, arc);
    }

    static void paintGlow(Graphics2D g2, int cx, int cy, int r, Color color, float alpha) {
        for(int i=r; i>0; i-=2) {
            float a = alpha * (float)i / r * 0.3f;
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255,(int)(a*255))));
            g2.fillOval(cx-i, cy-i, i*2, i*2);
        }
    }

    static void drawScanLine(Graphics2D g2, int y, int w, float alpha) {
        g2.setColor(new Color(0, 200, 255, (int)(alpha*40)));
        g2.fillRect(0, y, w, 2);
    }
}

// ==================== CITY MAP PANEL ====================
class CityMapPanel extends JPanel {
    private final CityManager manager;
    private double zoom = 1.0;
    private int offsetX = 80, offsetY = 70;
    private Point dragStart;
    private long animTick = 0;
    private javax.swing.Timer pulseTimer;

    CityMapPanel(CityManager manager) {
        this.manager = manager;
        setBackground(DS.BG_DEEP);
        setBorder(null);
        setupMouseListeners();
        pulseTimer = new javax.swing.Timer(50, e -> { animTick++; repaint(); });
        pulseTimer.start();
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragStart = e.getPoint(); }
        });
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if(dragStart!=null) { offsetX+=e.getX()-dragStart.x; offsetY+=e.getY()-dragStart.y; dragStart=e.getPoint(); repaint(); }
            }
        });
        addMouseWheelListener(e -> zoom(Math.max(0.5, Math.min(2.5, zoom*(e.getWheelRotation()>0?0.9:1.1)))));
    }

    public void zoom(double z) { zoom=z; repaint(); }
    public void resetView() { zoom=1.0; offsetX=80; offsetY=70; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cellSize = (int)(90 * zoom);
        paintBackground(g2);
        drawGrid(g2, cellSize);
        drawRoads(g2, cellSize);
        drawIntersections(g2, cellSize);
        drawEmergencyRoute(g2, cellSize);
        drawMarkers(g2, cellSize);
        drawVehicles(g2, cellSize);
        drawHUD(g2);
    }

    private void paintBackground(Graphics2D g2) {
        int w=getWidth(), h=getHeight();
        // Deep navy gradient
        GradientPaint gp = new GradientPaint(0,0,new Color(0x080E1A),w,h,new Color(0x0D1527));
        g2.setPaint(gp);
        g2.fillRect(0,0,w,h);
        // Dot grid
        g2.setColor(new Color(0x1A2440));
        for(int x=0;x<w;x+=20) for(int y=0;y<h;y+=20) g2.fillOval(x-1,y-1,2,2);
        // Animated scan line
        int scanY = (int)(animTick * 2) % (h+40) - 20;
        GradientPaint scan = new GradientPaint(0, scanY-10, new Color(0,200,255,0), 0, scanY, new Color(0,200,255,30), false);
        g2.setPaint(scan);
        g2.fillRect(0, scanY-10, w, 20);
    }

    private void drawGrid(Graphics2D g2, int cellSize) {
        // Background grid cells (subtle)
        for(int i=0;i<4;i++) for(int j=0;j<4;j++) {
            int x=offsetX+j*cellSize, y=offsetY+i*cellSize;
            g2.setColor(new Color(0x141E35));
            g2.fillRoundRect(x-cellSize/2+8, y-cellSize/2+8, cellSize-16, cellSize-16, 6, 6);
        }
    }

    private void drawRoads(Graphics2D g2, int cellSize) {
        int roadW = Math.max(6, (int)(14*zoom));
        for(int i=0;i<4;i++) for(int j=0;j<4;j++) {
            int x=offsetX+j*cellSize, y=offsetY+i*cellSize;
            String id=String.format("I%d%d",i,j);
            boolean blocked = manager.getGraph().containsKey(id) && manager.getGraph().get(id).isBlocked();

            // Road fill
            Color roadColor = blocked ? new Color(0x3D1010) : new Color(0x1A2744);
            Color roadEdge  = blocked ? new Color(0x6B0000) : new Color(0x2A3D5F);

            if(j<3) {
                int rx=x, ry=y-roadW/2;
                g2.setColor(roadColor); g2.fillRect(rx, ry, cellSize, roadW);
                g2.setColor(roadEdge); g2.drawRect(rx, ry, cellSize, roadW);
                // center dashes
                g2.setColor(new Color(0x2A3D5F));
                Stroke old=g2.getStroke();
                g2.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,1,new float[]{4,6},0));
                g2.drawLine(rx+5, y, rx+cellSize-5, y);
                g2.setStroke(old);
            }
            if(i<3) {
                int rx=x-roadW/2, ry=y;
                g2.setColor(roadColor); g2.fillRect(rx, ry, roadW, cellSize);
                g2.setColor(roadEdge); g2.drawRect(rx, ry, roadW, cellSize);
                Stroke old=g2.getStroke();
                g2.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,1,new float[]{4,6},0));
                g2.setColor(new Color(0x2A3D5F));
                g2.drawLine(x, ry+5, x, ry+cellSize-5);
                g2.setStroke(old);
            }
        }
    }

    private void drawIntersections(Graphics2D g2, int cellSize) {
        int r = Math.max(14, (int)(18*zoom));
        for(int i=0;i<4;i++) for(int j=0;j<4;j++) {
            String id=String.format("I%d%d",i,j);
            int x=offsetX+j*cellSize, y=offsetY+i*cellSize;
            boolean blocked = manager.getGraph().containsKey(id) && manager.getGraph().get(id).isBlocked();
            TrafficSignal ts = manager.getTrafficSignals().get(id);
            boolean emergency = ts != null && ts.isEmergencyActive();
            Incident inc = manager.getIncidentAtIntersection(id);

            if(inc!=null && !inc.resolved) {
                // Pulsing incident ring
                float pulse = (float)(Math.sin(animTick * 0.15) * 0.5 + 0.5);
                int pr = r + (int)(pulse*12);
                PaintHelper.paintGlow(g2, x, y, pr+6, inc.severity.getColor(), 0.6f);
                g2.setColor(new Color(inc.severity.getColor().getRed(), inc.severity.getColor().getGreen(),
                        inc.severity.getColor().getBlue(), 80));
                g2.fillOval(x-pr, y-pr, pr*2, pr*2);
            }

            if(emergency) {
                float pulse = (float)(Math.sin(animTick * 0.2) * 0.5 + 0.5);
                int er = r + (int)(pulse*8);
                g2.setColor(new Color(DS.DANGER.getRed(), DS.DANGER.getGreen(), DS.DANGER.getBlue(), 60));
                g2.fillOval(x-er-4, y-er-4, (er+4)*2, (er+4)*2);
            }

            // Node background glow
            Color nodeColor = blocked ? new Color(0x3D0000) : (emergency ? new Color(0x2D0010) : DS.BG_RAISED);
            Color nodeBorder = blocked ? DS.DANGER : (emergency ? DS.DISPATCH : DS.BORDER_ACTIVE);
            if(inc!=null&&!inc.resolved) nodeBorder = inc.severity.getColor();

            g2.setColor(nodeColor);
            g2.fillOval(x-r, y-r, r*2, r*2);
            g2.setColor(nodeBorder);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x-r, y-r, r*2, r*2);

            // ID label
            g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(9,(int)(11*zoom))));
            g2.setColor(blocked ? DS.DANGER : (inc!=null&&!inc.resolved ? inc.severity.getColor() : DS.TEXT_ACCENT));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(id, x - fm.stringWidth(id)/2, y + fm.getAscent()/2 - 1);

            // Block icon overlay
            if(blocked) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(8,(int)(10*zoom))));
                g2.setColor(DS.DANGER);
                g2.drawString("✕", x+r-6, y-r+10);
            }

            // Address label below (only for normal zoom)
            if(zoom >= 0.9) {
                Address addr = manager.getAddressBook().get(id);
                if(addr != null) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    g2.setColor(DS.TEXT_MUTED);
                    String lm = addr.landmark.length() > 10 ? addr.landmark.substring(0,9)+"…" : addr.landmark;
                    g2.drawString(lm, x - fm.stringWidth(lm)/2 + 2, y + r + 12);
                }
            }
        }
    }

    private void drawEmergencyRoute(Graphics2D g2, int cellSize) {
        List<String> route = manager.getActiveEmergencyRoute();
        if(route == null || route.isEmpty()) return;

        float dash = (float)((animTick * 2) % 20);
        // Glow pass
        g2.setColor(new Color(DS.DISPATCH.getRed(), DS.DISPATCH.getGreen(), DS.DISPATCH.getBlue(), 50));
        g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawRoutePath(g2, route, cellSize);

        // Main stroke — animated dash
        g2.setColor(DS.DISPATCH);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{10,8}, dash));
        drawRoutePath(g2, route, cellSize);

        // Route nodes
        for(String node : route) {
            int ni=Integer.parseInt(node.substring(1,2)), nj=Integer.parseInt(node.substring(2,3));
            int nx=offsetX+nj*cellSize, ny=offsetY+ni*cellSize;
            g2.setColor(DS.DISPATCH);
            g2.fillOval(nx-5, ny-5, 10, 10);
        }
    }

    private void drawRoutePath(Graphics2D g2, List<String> route, int cellSize) {
        for(int i=0;i<route.size()-1;i++) {
            String a=route.get(i), b=route.get(i+1);
            int ai=Integer.parseInt(a.substring(1,2)), aj=Integer.parseInt(a.substring(2,3));
            int bi=Integer.parseInt(b.substring(1,2)), bj=Integer.parseInt(b.substring(2,3));
            g2.drawLine(offsetX+aj*cellSize, offsetY+ai*cellSize, offsetX+bj*cellSize, offsetY+bi*cellSize);
        }
    }

    private void drawMarkers(Graphics2D g2, int cellSize) {
        for(MapMarker m : manager.getMarkers().values()) {
            int mi=Integer.parseInt(m.intersectionId.substring(1,2)), mj=Integer.parseInt(m.intersectionId.substring(2,3));
            int mx=offsetX+mj*cellSize, my=offsetY+mi*cellSize;
            // Pin stem
            float pulse=(float)(Math.sin(animTick*0.12)*0.5+0.5);
            int pinH = 30 + (int)(pulse*4);
            g2.setColor(m.color);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(mx, my-10, mx, my-pinH);
            // Pin head with glow
            PaintHelper.paintGlow(g2, mx, my-pinH-10, 16, m.color, 0.5f);
            g2.setColor(m.color);
            g2.fillOval(mx-12, my-pinH-22, 24, 24);
            g2.setColor(DS.BG_DEEP);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(mx-12, my-pinH-22, 24, 24);
            // Icon in pin
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString(m.icon, mx-7, my-pinH-5);
            // Label
            if(zoom >= 0.8) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.setColor(m.color);
                String label = m.label.length()>10 ? m.label.substring(0,9)+"…" : m.label;
                g2.drawString(label, mx-g2.getFontMetrics().stringWidth(label)/2, my-pinH-26);
            }
        }
    }

    private void drawVehicles(Graphics2D g2, int cellSize) {
        String dispatched = manager.getDispatchedVehicleId();
        for(Vehicle v : manager.getVehicles().values()) {
            int vx=offsetX+(int)(v.lon*cellSize), vy=offsetY+(int)(v.lat*cellSize);
            boolean isDispatched = v.id.equals(dispatched);
            boolean enRoute = v.getCurrentStatus().equals("En Route");
            Color vc = v.type.getColor();
            int vr = 11;

            if(isDispatched) {
                float pulse=(float)(Math.sin(animTick*0.25)*0.5+0.5);
                PaintHelper.paintGlow(g2, vx, vy, (int)(20+pulse*8), DS.DISPATCH, 0.7f);
            } else if(enRoute) {
                PaintHelper.paintGlow(g2, vx, vy, 18, vc, 0.5f);
            }

            // Vehicle dot
            g2.setColor(vc);
            g2.fillOval(vx-vr, vy-vr, vr*2, vr*2);
            g2.setColor(DS.BG_DEEP);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(vx-vr, vy-vr, vr*2, vr*2);

            // Icon
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.setColor(Color.WHITE);
            g2.drawString(v.type.getIcon(), vx-7, vy+5);

            // En route indicator dot
            if(enRoute) {
                g2.setColor(DS.WARN);
                g2.fillOval(vx+vr-4, vy-vr, 8, 8);
            }

            // ID label (hover-style, show always for emergency)
            if(v.emergency && zoom >= 0.9) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.setColor(vc);
                g2.drawString(v.id, vx-g2.getFontMetrics().stringWidth(v.id)/2, vy+vr+11);
            }
        }
    }

    private void drawHUD(Graphics2D g2) {
        // Mini legend bottom-left
        int lx=12, ly=getHeight()-80;
        g2.setColor(new Color(0,0,0,150));
        g2.fillRoundRect(lx-4, ly-4, 180, 76, 8, 8);
        g2.setColor(DS.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(lx-4, ly-4, 180, 76, 8, 8);
        g2.setFont(DS.FONT_BADGE);
        g2.setColor(DS.TEXT_MUTED);
        g2.drawString("LEGEND", lx, ly+10);
        int row=ly+24;
        Object[][] legend = {
                {DS.DISPATCH, "— Emergency Route"},
                {DS.DANGER, "● Active Incident"},
                {DS.TEXT_ACCENT, "● Intersection"},
                {DS.SAFE, "📍 Marker"}
        };
        for(Object[] item : legend) {
            g2.setColor((Color)item[0]);
            g2.fillRect(lx, row-7, 8, 8);
            g2.setColor(DS.TEXT_SECONDARY);
            g2.drawString((String)item[1], lx+12, row);
            row+=14;
        }

        // Zoom indicator top-right
        String zoomStr = String.format("%.0f%%", zoom*100);
        g2.setFont(DS.FONT_BADGE);
        g2.setColor(DS.TEXT_MUTED);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("ZOOM "+zoomStr, getWidth()-fm.stringWidth("ZOOM "+zoomStr)-10, 18);
    }
}

// ==================== STYLED INPUT DIALOG ====================
class StyledDialog {
    static String input(Component parent, String prompt, String title) {
        JPanel panel = new JPanel(new BorderLayout(0,8));
        panel.setBackground(DS.BG_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        JLabel lbl = new JLabel(prompt);
        lbl.setFont(DS.FONT_BODY);
        lbl.setForeground(DS.TEXT_PRIMARY);
        JTextField tf = new JTextField(22);
        tf.setBackground(DS.BG_RAISED);
        tf.setForeground(DS.TEXT_PRIMARY);
        tf.setCaretColor(DS.ACCENT_CYAN);
        tf.setFont(DS.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DS.BORDER_ACTIVE,1),
                BorderFactory.createEmptyBorder(6,10,6,10)));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(tf, BorderLayout.CENTER);
        UIManager.put("OptionPane.background", DS.BG_PANEL);
        UIManager.put("Panel.background", DS.BG_PANEL);
        int res = JOptionPane.showConfirmDialog(parent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return res == JOptionPane.OK_OPTION ? tf.getText().trim() : null;
    }

    static void info(Component parent, String msg, String title) {
        UIManager.put("OptionPane.background", DS.BG_PANEL);
        UIManager.put("Panel.background", DS.BG_PANEL);
        JOptionPane.showMessageDialog(parent, styleMsg(msg), title, JOptionPane.PLAIN_MESSAGE);
    }

    static void warn(Component parent, String msg, String title) {
        UIManager.put("OptionPane.background", DS.BG_PANEL);
        JOptionPane.showMessageDialog(parent, styleMsg(msg), title, JOptionPane.WARNING_MESSAGE);
    }

    static boolean confirm(Component parent, String msg, String title) {
        UIManager.put("OptionPane.background", DS.BG_PANEL);
        return JOptionPane.showConfirmDialog(parent, styleMsg(msg), title,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private static JLabel styleMsg(String msg) {
        JLabel l = new JLabel("<html><body style='width:300px;color:#E8EDFB;font-family:Segoe UI'>" +
                msg.replace("\n","<br>") + "</body></html>");
        l.setFont(DS.FONT_BODY);
        l.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        return l;
    }

    static <T> T combo(Component parent, String prompt, String title, T[] options, T def) {
        UIManager.put("OptionPane.background", DS.BG_PANEL);
        UIManager.put("Panel.background", DS.BG_PANEL);
        return (T)JOptionPane.showInputDialog(parent, prompt, title,
                JOptionPane.QUESTION_MESSAGE, null, options, def);
    }
}

// ==================== COMMAND BUTTON ====================
class CmdButton extends JButton {
    private final Color accent;
    private float hoverAlpha = 0f;
    private javax.swing.Timer hoverTimer;

    CmdButton(String text, Color accent) {
        super(text);
        this.accent = accent;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        setFont(DS.FONT_LABEL);
        setForeground(DS.TEXT_PRIMARY);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { animateHover(true); }
            public void mouseExited(MouseEvent e) { animateHover(false); }
        });
    }

    private void animateHover(boolean in) {
        if(hoverTimer!=null) hoverTimer.stop();
        hoverTimer = new javax.swing.Timer(16, e -> {
            hoverAlpha = in ? Math.min(1f, hoverAlpha+0.1f) : Math.max(0f, hoverAlpha-0.1f);
            repaint();
            if((in && hoverAlpha>=1f) || (!in && hoverAlpha<=0f)) hoverTimer.stop();
        });
        hoverTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(), h=getHeight();

        // Base
        g2.setColor(DS.BG_RAISED);
        g2.fillRoundRect(0,0,w,h,8,8);

        // Accent left bar
        g2.setColor(accent);
        g2.fillRoundRect(0,0,3,h,2,2);

        // Hover fill
        if(hoverAlpha > 0f) {
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(hoverAlpha*30)));
            g2.fillRoundRect(0,0,w,h,8,8);
        }

        // Border
        g2.setColor(new Color(DS.BORDER_ACTIVE.getRed(), DS.BORDER_ACTIVE.getGreen(),
                DS.BORDER_ACTIVE.getBlue(), (int)(80+hoverAlpha*120)));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0,0,w-1,h-1,8,8);

        // Pressed state
        if(getModel().isPressed()) {
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50));
            g2.fillRoundRect(0,0,w,h,8,8);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}

// ==================== STAT CARD ====================
class StatCard extends JPanel {
    private final String label;
    private String value;
    private final Color accent;
    private String subtext = "";

    StatCard(String label, String value, Color accent) {
        this.label = label; this.value = value; this.accent = accent;
        setOpaque(false);
        setPreferredSize(new Dimension(130, 80));
    }

    void setValue(String v) { this.value=v; repaint(); }
    void setSubtext(String s) { this.subtext=s; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(), h=getHeight();

        // Card bg
        g2.setColor(DS.BG_PANEL);
        g2.fillRoundRect(0,0,w,h,10,10);
        g2.setColor(DS.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0,0,w-1,h-1,10,10);

        // Accent top bar
        g2.setColor(accent);
        g2.fillRoundRect(0,0,w,4,4,4);

        // Value
        g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
        g2.setColor(DS.TEXT_PRIMARY);
        FontMetrics fmV = g2.getFontMetrics();
        g2.drawString(value, (w-fmV.stringWidth(value))/2, 42);

        // Label
        g2.setFont(DS.FONT_BADGE);
        g2.setColor(DS.TEXT_SECONDARY);
        FontMetrics fmL = g2.getFontMetrics();
        g2.drawString(label.toUpperCase(), (w-fmL.stringWidth(label.toUpperCase()))/2, 58);

        // Subtext
        if(!subtext.isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.setColor(DS.TEXT_MUTED);
            g2.drawString(subtext, (w-g2.getFontMetrics().stringWidth(subtext))/2, 72);
        }
    }
}

// ==================== STATS PANEL ====================
class StatsPanel extends JPanel {
    private final CityManager manager;
    private final StatCard scVehicles, scEmergency, scAvail, scEnRoute, scIncidents, scBlocked, scMarkers;

    StatsPanel(CityManager manager) {
        this.manager = manager;
        setBackground(DS.BG_BASE);
        setLayout(new BorderLayout(0,12));
        setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        JLabel title = new JLabel("SYSTEM STATUS");
        title.setFont(DS.FONT_LABEL);
        title.setForeground(DS.TEXT_MUTED);
        title.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));

        JPanel grid = new JPanel(new GridLayout(0,2,10,10));
        grid.setOpaque(false);

        scVehicles  = new StatCard("Vehicles",   "0", DS.ACCENT_CYAN);
        scEmergency = new StatCard("Emergency",  "0", DS.DANGER);
        scAvail     = new StatCard("Available",  "0", DS.SAFE);
        scEnRoute   = new StatCard("En Route",   "0", DS.DISPATCH);
        scIncidents = new StatCard("Incidents",  "0", DS.WARN);
        scBlocked   = new StatCard("Blocked",    "0", DS.DANGER);
        scMarkers   = new StatCard("Markers",    "0", DS.ACCENT_VIOLET);

        for(StatCard sc : new StatCard[]{scVehicles,scEmergency,scAvail,scEnRoute,scIncidents,scBlocked,scMarkers})
            grid.add(sc);

        // Live clock card
        StatCard scClock = new StatCard("LIVE", "", DS.ACCENT_TEAL) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Override value with time
            }
        };

        add(title, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);

        // System health bar
        add(createHealthPanel(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel createHealthPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(); int h=getHeight();
                g2.setColor(DS.BG_PANEL);
                g2.fillRoundRect(0,0,w,h,8,8);
                g2.setColor(DS.BORDER_SUBTLE);
                g2.drawRoundRect(0,0,w-1,h-1,8,8);
                Map<String,Object> status = manager.getSystemStatus();
                long avail = (Long)status.get("available_emergency");
                long total = (Long)status.get("emergency_vehicles");
                double ratio = total>0 ? (double)avail/total : 1.0;
                Color hColor = ratio>0.6 ? DS.SAFE : (ratio>0.3 ? DS.WARN : DS.DANGER);
                g2.setColor(DS.BG_RAISED);
                g2.fillRoundRect(8,26,w-16,8,4,4);
                g2.setColor(hColor);
                g2.fillRoundRect(8,26,(int)((w-16)*ratio),8,4,4);
                g2.setFont(DS.FONT_BADGE);
                g2.setColor(DS.TEXT_MUTED);
                g2.drawString("FLEET READINESS", 8, 20);
                g2.setColor(hColor);
                String pct = String.format("%.0f%%", ratio*100);
                g2.drawString(pct, w-g2.getFontMetrics().stringWidth(pct)-8, 20);
            }
        };
        p.setPreferredSize(new Dimension(200,42));
        p.setOpaque(false);
        return p;
    }

    void refresh() {
        Map<String,Object> s = manager.getSystemStatus();
        scVehicles.setValue(String.valueOf(s.get("active_vehicles")));
        scEmergency.setValue(String.valueOf(s.get("emergency_vehicles")));
        scAvail.setValue(String.valueOf(s.get("available_emergency")));
        scEnRoute.setValue(String.valueOf(s.get("enroute_vehicles")));
        scIncidents.setValue(String.valueOf(s.get("active_incidents")));
        scBlocked.setValue(String.valueOf(s.get("blocked_roads")));
        scMarkers.setValue(String.valueOf(s.get("markers_count")));
        repaint();
    }
}

// ==================== INCIDENT PANEL ====================
class IncidentPanel extends JPanel {
    private final CityManager manager;
    private final DefaultListModel<Incident> listModel = new DefaultListModel<>();
    private final JList<Incident> incidentList;

    IncidentPanel(CityManager manager) {
        this.manager = manager;
        setBackground(DS.BG_BASE);
        setLayout(new BorderLayout(0,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel title = new JLabel("ACTIVE INCIDENTS");
        title.setFont(DS.FONT_LABEL);
        title.setForeground(DS.TEXT_MUTED);

        incidentList = new JList<>(listModel);
        incidentList.setBackground(DS.BG_PANEL);
        incidentList.setForeground(DS.TEXT_PRIMARY);
        incidentList.setSelectionBackground(DS.BG_HOVER);
        incidentList.setSelectionForeground(DS.TEXT_PRIMARY);
        incidentList.setFixedCellHeight(56);
        incidentList.setBorder(null);
        incidentList.setCellRenderer(new IncidentRenderer());

        JScrollPane sp = new JScrollPane(incidentList);
        sp.setBorder(BorderFactory.createLineBorder(DS.BORDER_SUBTLE, 1));
        sp.getViewport().setBackground(DS.BG_PANEL);
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI());

        CmdButton resolveBtn = new CmdButton("Mark Selected Resolved", DS.SAFE);
        resolveBtn.addActionListener(e -> resolve());

        add(title, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(resolveBtn, BorderLayout.SOUTH);
        refresh();
    }

    void refresh() {
        listModel.clear();
        for(Incident i : manager.getAllIncidents()) if(!i.resolved) listModel.addElement(i);
    }

    private void resolve() {
        Incident sel = incidentList.getSelectedValue();
        if(sel==null){StyledDialog.warn(this,"Select an incident first.","No Selection");return;}
        if(StyledDialog.confirm(this,
                "Resolve incident at "+sel.location+"?\n\nSeverity: "+sel.severity.getDisplayName()+
                        "\n"+sel.description+"\n\nThis will unblock the intersection.", "Confirm Resolution")) {
            manager.resolveIncident(sel.location);
            listModel.removeElement(sel);
            SwingUtilities.getWindowAncestor(this).repaint();
        }
    }

    private class IncidentRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object val, int idx, boolean sel, boolean focus) {
            JPanel p = new JPanel(new BorderLayout(8,0));
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,1,0,DS.BORDER_SUBTLE),
                    BorderFactory.createEmptyBorder(8,10,8,10)));
            p.setBackground(sel ? DS.BG_HOVER : DS.BG_PANEL);

            if(val instanceof Incident inc) {
                // Severity badge
                JLabel badge = new JLabel(" "+inc.severity.getDisplayName().toUpperCase()+" ");
                badge.setFont(DS.FONT_BADGE);
                badge.setOpaque(true);
                badge.setBackground(new Color(inc.severity.getColor().getRed(), inc.severity.getColor().getGreen(),
                        inc.severity.getColor().getBlue(), 40));
                badge.setForeground(inc.severity.getColor());
                badge.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(inc.severity.getColor().getRed(),
                                inc.severity.getColor().getGreen(), inc.severity.getColor().getBlue(), 100), 1),
                        BorderFactory.createEmptyBorder(2,5,2,5)));

                JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                left.setOpaque(false);
                left.add(badge);

                JLabel loc = new JLabel(inc.location);
                loc.setFont(DS.FONT_TITLE);
                loc.setForeground(DS.TEXT_PRIMARY);

                JLabel desc = new JLabel(inc.description.length()>45?inc.description.substring(0,44)+"…":inc.description);
                desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                desc.setForeground(DS.TEXT_SECONDARY);

                JLabel time = new JLabel(inc.getFormattedTime());
                time.setFont(DS.FONT_BADGE);
                time.setForeground(DS.TEXT_MUTED);

                JPanel info = new JPanel(new BorderLayout(0,2));
                info.setOpaque(false);
                JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
                top.setOpaque(false);
                top.add(loc); top.add(badge); top.add(time);
                info.add(top, BorderLayout.NORTH);
                info.add(desc, BorderLayout.CENTER);

                // Left severity stripe
                JPanel stripe = new JPanel();
                stripe.setBackground(inc.severity.getColor());
                stripe.setPreferredSize(new Dimension(4,0));

                p.add(stripe, BorderLayout.WEST);
                p.add(info, BorderLayout.CENTER);
            }
            return p;
        }
    }
}

// ==================== VEHICLE PANEL ====================
class VehiclePanel extends JPanel {
    private final CityManager manager;
    private final DefaultListModel<Vehicle> listModel = new DefaultListModel<>();
    private final JList<Vehicle> vehicleList;
    private final JTextArea detailsArea;

    VehiclePanel(CityManager manager) {
        this.manager = manager;
        setBackground(DS.BG_BASE);
        setLayout(new BorderLayout(0,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        vehicleList = new JList<>(listModel);
        vehicleList.setBackground(DS.BG_PANEL);
        vehicleList.setFixedCellHeight(44);
        vehicleList.setBorder(null);
        vehicleList.setCellRenderer(new VehicleRenderer());
        vehicleList.addListSelectionListener(e -> showDetails());

        JScrollPane listSP = new JScrollPane(vehicleList);
        listSP.setBorder(BorderFactory.createLineBorder(DS.BORDER_SUBTLE,1));
        listSP.getViewport().setBackground(DS.BG_PANEL);
        listSP.getVerticalScrollBar().setUI(new SlimScrollBarUI());

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(DS.FONT_MONO);
        detailsArea.setBackground(new Color(0x080D1A));
        detailsArea.setForeground(DS.ACCENT_CYAN);
        detailsArea.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
        detailsArea.setText("Select a vehicle to view details.");

        JScrollPane detailSP = new JScrollPane(detailsArea);
        detailSP.setBorder(BorderFactory.createLineBorder(DS.BORDER_SUBTLE,1));
        detailSP.getVerticalScrollBar().setUI(new SlimScrollBarUI());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listSP, detailSP);
        split.setDividerLocation(260);
        split.setDividerSize(4);
        split.setBorder(null);
        split.setBackground(DS.BG_BASE);

        JLabel title = new JLabel("FLEET REGISTRY");
        title.setFont(DS.FONT_LABEL);
        title.setForeground(DS.TEXT_MUTED);
        title.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));

        add(title, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        refresh();
    }

    void refresh() {
        listModel.clear();
        // Sort: emergency first, then by status
        manager.getVehicles().values().stream()
                .sorted(Comparator.comparing((Vehicle v) -> !v.emergency)
                        .thenComparing(v -> v.getCurrentStatus().equals("En Route") ? 0 : 1)
                        .thenComparing(v -> v.id))
                .forEach(listModel::addElement);
    }

    private void showDetails() {
        Vehicle v = vehicleList.getSelectedValue();
        if(v==null) return;
        String details = String.format(
                "ID           %s%n" +
                        "TYPE         %s %s%n" +
                        "EMERGENCY    %s%n" +
                        "STATUS       %s%n" +
                        "LOCATION     (%.2f, %.2f)%n" +
                        "DESTINATION  %s%n" +
                        "─────────────────────%n" +
                        "Last update: recent",
                v.id, v.type.getIcon(), v.type.getDisplayName(),
                v.emergency ? "YES" : "NO",
                v.getCurrentStatus(),
                v.lat, v.lon,
                v.getDestination().isEmpty() ? "None" : v.getDestination()
        );
        detailsArea.setText(details);
        detailsArea.setCaretPosition(0);
    }

    private class VehicleRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object val, int idx, boolean sel, boolean focus) {
            JPanel p = new JPanel(new BorderLayout(10,0));
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,1,0,DS.BORDER_SUBTLE),
                    BorderFactory.createEmptyBorder(6,10,6,10)));
            p.setBackground(sel ? DS.BG_HOVER : DS.BG_PANEL);

            if(val instanceof Vehicle v) {
                JLabel iconLbl = new JLabel(v.type.getIcon());
                iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 20));
                iconLbl.setPreferredSize(new Dimension(28,28));

                JLabel nameLbl = new JLabel(v.id + "  " + v.type.getDisplayName());
                nameLbl.setFont(DS.FONT_LABEL);
                nameLbl.setForeground(v.type.getColor());

                String statusIcon = v.getCurrentStatus().equals("En Route") ? "▶ " : (v.getCurrentStatus().equals("Available") ? "● " : "⏸ ");
                Color statusColor = v.getCurrentStatus().equals("En Route") ? DS.DISPATCH :
                        (v.getCurrentStatus().equals("Available") ? DS.SAFE : DS.TEXT_MUTED);
                JLabel statusLbl = new JLabel(statusIcon + v.getCurrentStatus());
                statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                statusLbl.setForeground(statusColor);

                JPanel info = new JPanel(new BorderLayout(0,2));
                info.setOpaque(false);
                info.add(nameLbl, BorderLayout.NORTH);
                info.add(statusLbl, BorderLayout.CENTER);

                // Left type stripe
                JPanel stripe = new JPanel();
                stripe.setBackground(v.type.getColor());
                stripe.setPreferredSize(new Dimension(3,0));

                p.add(stripe, BorderLayout.WEST);
                p.add(iconLbl, BorderLayout.WEST);
                p.add(info, BorderLayout.CENTER);

                if(v.emergency) {
                    JLabel badge = new JLabel("EMG");
                    badge.setFont(DS.FONT_BADGE);
                    badge.setForeground(DS.DANGER);
                    badge.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(DS.DANGER.getRed(), DS.DANGER.getGreen(), DS.DANGER.getBlue(), 80), 1),
                            BorderFactory.createEmptyBorder(1,4,1,4)));
                    p.add(badge, BorderLayout.EAST);
                }
            }
            return p;
        }
    }
}

// ==================== TRAFFIC SIGNAL PANEL ====================
class TrafficSignalPanel extends JPanel {
    private final CityManager manager;

    TrafficSignalPanel(CityManager manager) {
        this.manager = manager;
        setBackground(DS.BG_BASE);
        setLayout(new BorderLayout(0,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel title = new JLabel("SIGNAL GRID  —  4 × 4");
        title.setFont(DS.FONT_LABEL);
        title.setForeground(DS.TEXT_MUTED);

        add(title, BorderLayout.NORTH);
        add(createGrid(), BorderLayout.CENTER);

        new javax.swing.Timer(800, e -> { removeAll(); add(title, BorderLayout.NORTH); add(createGrid(), BorderLayout.CENTER); revalidate(); repaint(); }).start();
    }

    private JPanel createGrid() {
        JPanel grid = new JPanel(new GridLayout(4, 4, 8, 8));
        grid.setOpaque(false);
        for(int i=0;i<4;i++) for(int j=0;j<4;j++) {
            String id=String.format("I%d%d",i,j);
            grid.add(createSignalNode(id, manager.getTrafficSignals().get(id)));
        }
        return grid;
    }

    private JPanel createSignalNode(String id, TrafficSignal ts) {
        boolean em = ts != null && ts.isEmergencyActive();
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(), h=getHeight();

                // Background
                g2.setColor(em ? new Color(0x200808) : DS.BG_PANEL);
                g2.fillRoundRect(0,0,w,h,10,10);
                g2.setColor(em ? DS.DANGER : DS.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(em?1.5f:1f));
                g2.drawRoundRect(0,0,w-1,h-1,10,10);

                // Signal dot
                Color sigColor = em ? DS.DANGER : DS.SAFE;
                int sr=8;
                if(em) {
                    g2.setColor(new Color(DS.DANGER.getRed(),DS.DANGER.getGreen(),DS.DANGER.getBlue(),60));
                    g2.fillOval(w/2-sr-4,h/2-sr-8,sr*2+8,sr*2+8);
                }
                g2.setColor(sigColor);
                g2.fillOval(w/2-sr,h/2-sr-4,sr*2,sr*2);

                // ID
                g2.setFont(DS.FONT_BADGE);
                g2.setColor(em ? DS.DANGER : DS.TEXT_SECONDARY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(id, (w-fm.stringWidth(id))/2, h-8);

                // Status text
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                g2.setColor(em ? new Color(0xFF8888) : new Color(0x006644));
                String st = em ? "OVERRIDE" : "NORMAL";
                g2.drawString(st, (w-g2.getFontMetrics().stringWidth(st))/2, h/2+14);
            }
            { setPreferredSize(new Dimension(60,70)); setOpaque(false); }
        };
    }
}

// ==================== LOG PANEL ====================
class LogPanel extends JPanel {
    private final JTextArea logArea;

    LogPanel() {
        setBackground(DS.BG_BASE);
        setLayout(new BorderLayout(0,6));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JLabel title = new JLabel("SYSTEM LOG");
        title.setFont(DS.FONT_LABEL);
        title.setForeground(DS.TEXT_MUTED);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(0x060B16));
        logArea.setForeground(DS.ACCENT_CYAN);
        logArea.setCaretColor(DS.ACCENT_CYAN);
        logArea.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));

        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(BorderFactory.createLineBorder(DS.BORDER_SUBTLE, 1));
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        sp.getViewport().setBackground(new Color(0x060B16));

        add(title, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    void append(String msg) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("["+ts+"] "+msg+"\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}

// ==================== SLIM SCROLLBAR ====================
class SlimScrollBarUI extends BasicScrollBarUI {
    @Override protected void configureScrollBarColors() {
        thumbColor = DS.BORDER_ACTIVE;
        trackColor = DS.BG_BASE;
    }
    @Override protected JButton createDecreaseButton(int o) { return makeZero(); }
    @Override protected JButton createIncreaseButton(int o) { return makeZero(); }
    private JButton makeZero() {
        JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); return b;
    }
    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle tr) {
        Graphics2D g2=(Graphics2D)g; g2.setColor(thumbColor);
        g2.fillRoundRect(tr.x+2,tr.y+2,tr.width-4,tr.height-4,6,6);
    }
    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle tr) {
        g.setColor(trackColor); g.fillRect(tr.x,tr.y,tr.width,tr.height);
    }
}

// ==================== CONTROL PANEL ====================
class ControlPanel extends JPanel {
    private final CityManager manager;
    private final CityMapPanel mapPanel;
    private final LogPanel logPanel;
    private final JFrame parentFrame;

    ControlPanel(CityManager manager, CityMapPanel mapPanel, LogPanel logPanel, JFrame parent) {
        this.manager=manager; this.mapPanel=mapPanel; this.logPanel=logPanel; this.parentFrame=parent;
        setBackground(DS.BG_BASE);
        setLayout(new BorderLayout(0,0));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel btnPanel = new JPanel(new GridLayout(0,1,0,5));
        btnPanel.setOpaque(false);

        addSection(btnPanel, "NAVIGATION", new String[][]{
                {"Search & Mark Address", "SEARCH"},
                {"Plan Route", "ROUTE"},
                {"Find Alternate Routes", "ALT_ROUTE"}
        });
        addSection(btnPanel, "EMERGENCY", new String[][]{
                {"Report Incident", "INCIDENT"},
                {"Auto-Dispatch Emergency", "DISPATCH_AUTO"},
                {"Dispatch Specific Vehicle", "DISPATCH_SPEC"},
                {"Complete Mission", "COMPLETE"}
        });
        addSection(btnPanel, "MANAGEMENT", new String[][]{
                {"Simulate Traffic", "SIMULATE"},
                {"Block / Unblock Road", "BLOCK"},
                {"Manage Markers", "MARKERS"},
                {"Clear Resolved Incidents", "CLEAR"},
                {"Export Data", "EXPORT"}
        });

        add(new JScrollPane(btnPanel) {{ setBorder(null); getViewport().setBackground(DS.BG_BASE);
            getVerticalScrollBar().setUI(new SlimScrollBarUI()); }}, BorderLayout.CENTER);
    }

    private void addSection(JPanel parent, String sectionTitle, String[][] actions) {
        JLabel sec = new JLabel(sectionTitle);
        sec.setFont(DS.FONT_BADGE);
        sec.setForeground(DS.TEXT_MUTED);
        sec.setBorder(BorderFactory.createEmptyBorder(10,4,4,0));
        parent.add(sec);

        for(String[] action : actions) {
            Color accent = sectionTitle.equals("EMERGENCY") ? DS.DANGER :
                    sectionTitle.equals("NAVIGATION") ? DS.ACCENT_CYAN : DS.ACCENT_VIOLET;
            if(action[1].equals("DISPATCH_AUTO")||action[1].equals("DISPATCH_SPEC")) accent = DS.DISPATCH;
            if(action[1].equals("COMPLETE")) accent = DS.SAFE;
            CmdButton btn = new CmdButton(action[0], accent);
            final String cmd = action[1];
            btn.addActionListener(e -> handleAction(cmd));
            parent.add(btn);
        }
    }

    private void handleAction(String cmd) {
        switch(cmd) {
            case "SEARCH" -> searchAndMark();
            case "ROUTE" -> planRoute();
            case "ALT_ROUTE" -> findAlternates();
            case "INCIDENT" -> reportIncident();
            case "DISPATCH_AUTO" -> dispatchAuto();
            case "DISPATCH_SPEC" -> dispatchSpecific();
            case "COMPLETE" -> completeDispatch();
            case "SIMULATE" -> simulate();
            case "BLOCK" -> blockUnblock();
            case "MARKERS" -> manageMarkers();
            case "CLEAR" -> clearIncidents();
            case "EXPORT" -> export();
        }
    }

    private void searchAndMark() {
        String query = StyledDialog.input(this, "Street name, landmark, or intersection ID:", "Search Address");
        if(query==null||query.isEmpty()) return;
        String found = manager.searchAddress(query);
        if(found==null) found = query.toUpperCase();
        Address addr = manager.getAddressBook().get(found);
        if(addr==null) { StyledDialog.warn(this, "Location '"+query+"' not found.", "Not Found"); return; }
        String label = StyledDialog.input(this, "Marker label:", "Add Marker");
        if(label==null||label.isEmpty()) label = addr.landmark;
        String[] icons = {"📍","⭐","🏥","🏠","⚠️","🔰","💎","🅿️","⛽","🏪"};
        String icon = StyledDialog.combo(this, "Select icon:", "Marker Icon", icons, icons[0]);
        if(icon==null) icon="📍";
        Color[] palette = {DS.ACCENT_CYAN, DS.DANGER, DS.SAFE, DS.DISPATCH, DS.ACCENT_VIOLET, DS.ACCENT_TEAL};
        Color color = StyledDialog.combo(this, "Select color:", "Marker Color", palette, palette[0]);
        if(color==null) color = DS.ACCENT_CYAN;
        manager.addMarker(found, label, color, icon);
        mapPanel.repaint();
        log("SEARCH   Marked '"+label+"' at "+found+" ("+addr.streetName+")");
        StyledDialog.info(this, "Marker placed at "+addr.getFullAddress(), "Marker Added");
    }

    private void manageMarkers() {
        Map<String,MapMarker> markers = manager.getMarkers();
        if(markers.isEmpty()){StyledDialog.info(this,"No markers on map.","Markers");return;}
        MapMarker[] markerArr = markers.values().toArray(new MapMarker[0]);
        String[] options = java.util.Arrays.stream(markerArr).map(m->m.icon+" "+m.label+" ["+m.intersectionId+"]").toArray(String[]::new);
        String[] actions = {"Remove Selected", "Clear All", "Cancel"};
        int act = JOptionPane.showOptionDialog(this, "Manage "+markers.size()+" markers:", "Markers",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, actions, actions[2]);
        if(act==0) {
            String sel = StyledDialog.combo(this, "Select marker to remove:", "Remove", options, options[0]);
            if(sel!=null) {
                int idx = java.util.Arrays.asList(options).indexOf(sel);
                if(idx>=0){manager.removeMarker(markerArr[idx].id);mapPanel.repaint();log("MARKER  Removed");}
            }
        } else if(act==1) {
            if(StyledDialog.confirm(this,"Remove all markers?","Confirm")) {
                manager.clearAllMarkers(); mapPanel.repaint(); log("MARKER  All cleared");
            }
        }
    }

    private void planRoute() {
        String s = StyledDialog.input(this, "Start (intersection or address):", "Plan Route"); if(s==null)return;
        String start = resolveLocation(s);
        String e = StyledDialog.input(this, "Destination (intersection or address):", "Plan Route"); if(e==null)return;
        String end = resolveLocation(e);
        DijkstraResult res = manager.findShortestPath(start,end);
        if(res.isPathFound()) {
            manager.setActiveEmergencyRoute(res.path); mapPanel.repaint();
            log("ROUTE    "+start+" → "+end+" ("+String.format("%.2f",res.distance)+" km) "+res.path.size()+" nodes");
            StyledDialog.info(this, "Route: "+String.join(" → ",res.path)+"\n\nDistance: "+String.format("%.2f km",res.distance), "Route Found");
        } else { StyledDialog.warn(this,"No route found. Check for blocked roads.","No Route"); }
    }

    private void findAlternates() {
        String s=StyledDialog.input(this,"Start:","Alternate Routes"); if(s==null)return;
        String start=resolveLocation(s);
        String e=StyledDialog.input(this,"Destination:","Alternate Routes"); if(e==null)return;
        String end=resolveLocation(e);
        String nStr=StyledDialog.input(this,"Number of routes (1–10):","Alternate Routes");
        int n=5; try{n=Math.min(10,Math.max(1,Integer.parseInt(nStr==null?"5":nStr)));}catch(Exception ex){}
        final String fs=start,fe=end; final int fn=n;
        JDialog loading=new JDialog(parentFrame,"Finding routes…",false);
        JLabel lbl=new JLabel("Calculating "+fn+" routes…",SwingConstants.CENTER);
        lbl.setFont(DS.FONT_BODY); lbl.setForeground(DS.TEXT_PRIMARY);
        lbl.setBorder(BorderFactory.createEmptyBorder(20,30,20,30));
        loading.add(lbl); loading.setSize(280,80); loading.setLocationRelativeTo(this);
        SwingWorker<List<RouteInfo>,Void> worker=new SwingWorker<>(){
            @Override protected List<RouteInfo> doInBackground(){return manager.findAllAlternateRoutes(fs,fe,fn);}
            @Override protected void done(){
                loading.dispose();
                try {
                    List<RouteInfo> routes=get();
                    if(routes.isEmpty()){StyledDialog.warn(ControlPanel.this,"No routes found.","No Routes");return;}
                    showRouteSelector(routes, fs, fe);
                } catch(Exception ex){StyledDialog.warn(ControlPanel.this,"Error: "+ex.getMessage(),"Error");}
            }
        };
        worker.execute(); loading.setVisible(true);
    }

    private void showRouteSelector(List<RouteInfo> routes, String start, String end) {
        JDialog dlg=new JDialog(parentFrame,"Alternate Routes ("+routes.size()+" found)",true);
        dlg.setSize(700,500); dlg.setLocationRelativeTo(parentFrame);
        JPanel main=new JPanel(new BorderLayout(0,10));
        main.setBackground(DS.BG_BASE);
        main.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        DefaultListModel<RouteInfo> model=new DefaultListModel<>();
        routes.forEach(model::addElement);
        JList<RouteInfo> list=new JList<>(model);
        list.setBackground(DS.BG_PANEL);
        list.setForeground(DS.TEXT_PRIMARY);
        list.setSelectionBackground(DS.BG_HOVER);
        list.setFixedCellHeight(40);
        list.setCellRenderer((l,v,i,s,f)->{
            JPanel p=new JPanel(new BorderLayout(10,0));
            p.setBackground(s?DS.BG_HOVER:DS.BG_PANEL);
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,1,0,DS.BORDER_SUBTLE),
                    BorderFactory.createEmptyBorder(6,10,6,10)));
            RouteInfo ri=(RouteInfo)v;
            JLabel num=new JLabel("R"+ri.routeNumber);
            num.setFont(DS.FONT_BADGE); num.setForeground(DS.ACCENT_VIOLET);
            num.setPreferredSize(new Dimension(26,0));
            JLabel path=new JLabel(String.join(" → ",ri.path));
            path.setFont(DS.FONT_BODY); path.setForeground(DS.TEXT_PRIMARY);
            JLabel dist=new JLabel(String.format("%.2f km",ri.distance));
            dist.setFont(DS.FONT_BADGE); dist.setForeground(DS.ACCENT_TEAL);
            p.add(num,BorderLayout.WEST); p.add(path,BorderLayout.CENTER); p.add(dist,BorderLayout.EAST);
            return p;
        });
        JScrollPane sp=new JScrollPane(list);
        sp.setBorder(BorderFactory.createLineBorder(DS.BORDER_SUBTLE,1));
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI());

        CmdButton sel=new CmdButton("Use Selected Route",DS.SAFE);
        sel.addActionListener(e->{
            RouteInfo r=list.getSelectedValue();
            if(r!=null){manager.setActiveEmergencyRoute(r.path);mapPanel.repaint();
                log("ROUTE    Selected alternate route "+r.routeNumber+" ("+String.format("%.2f",r.distance)+" km)");dlg.dispose();}
        });
        JPanel btns=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        CmdButton cancel=new CmdButton("Cancel",DS.TEXT_MUTED);
        cancel.addActionListener(e->dlg.dispose());
        btns.add(sel); btns.add(cancel);
        main.add(sp,BorderLayout.CENTER); main.add(btns,BorderLayout.SOUTH);
        dlg.add(main); dlg.setVisible(true);
        log("ROUTE    Found "+routes.size()+" alternate routes "+start+"→"+end);
    }

    private void reportIncident() {
        String loc=StyledDialog.input(this,"Intersection ID or address:","Report Incident"); if(loc==null)return;
        loc=resolveLocation(loc);
        Severity sev=StyledDialog.combo(this,"Severity:","Incident Severity",Severity.values(),Severity.MEDIUM);
        if(sev==null)return;
        String desc=StyledDialog.input(this,"Description:","Incident Details");
        if(desc==null)desc="No description";
        manager.reportIncident(loc,sev,desc);
        mapPanel.repaint();
        log("INCIDENT "+sev.getDisplayName()+" at "+loc+": "+desc);
    }

    private void dispatchAuto() {
        String loc=StyledDialog.input(this,"Emergency location:","Auto Dispatch"); if(loc==null)return;
        loc=resolveLocation(loc);
        Vehicle v=manager.dispatchClosestAmbulance(loc);
        if(v==null){StyledDialog.warn(this,"No available emergency vehicles.","Unavailable");log("DISPATCH No vehicles available");return;}
        boolean ok=manager.dispatchSpecificVehicle(v.id,loc);
        if(ok){mapPanel.repaint();log("DISPATCH AUTO  "+v.type.getIcon()+" "+v.id+" → "+loc);
            StyledDialog.info(this,v.type.getIcon()+" "+v.id+" dispatched to "+loc+"\nRoute calculated. Traffic signals overridden.","Dispatched");}
        else StyledDialog.warn(this,"Dispatch failed.","Error");
    }

    private void dispatchSpecific() {
        List<Vehicle> avail=manager.getAvailableEmergencyVehicles();
        if(avail.isEmpty()){StyledDialog.warn(this,"No emergency vehicles available.","Unavailable");return;}
        String[] opts=avail.stream().map(v->v.type.getIcon()+" "+v.id+" — "+v.type.getDisplayName()+" @ I"+(int)v.lat+""+(int)v.lon).toArray(String[]::new);
        String sel=StyledDialog.combo(this,"Select vehicle:","Dispatch Vehicle",opts,opts[0]); if(sel==null)return;
        String vid=sel.split(" ")[1];
        String loc=StyledDialog.input(this,"Destination:","Dispatch Vehicle"); if(loc==null)return;
        loc=resolveLocation(loc);
        boolean ok=manager.dispatchSpecificVehicle(vid,loc);
        if(ok){mapPanel.repaint();log("DISPATCH SPEC  "+vid+" → "+loc);
            StyledDialog.info(this,vid+" dispatched to "+loc,"Dispatched");}
        else StyledDialog.warn(this,"Dispatch failed — check if destination is reachable.","Failed");
    }

    private void completeDispatch() {
        String did=manager.getDispatchedVehicleId();
        if(did==null){StyledDialog.info(this,"No active dispatch mission.","Complete");return;}
        if(StyledDialog.confirm(this,"Mark mission for "+did+" complete?","Complete Mission")) {
            manager.completeDispatch(); mapPanel.repaint(); log("COMPLETE "+did+" mission complete");
        }
    }

    private void simulate() {
        manager.simulateTraffic(); mapPanel.repaint(); log("TRAFFIC  Simulation tick");
    }

    private void blockUnblock() {
        String[] options={"Block Road","Unblock Road","Cancel"};
        int choice=JOptionPane.showOptionDialog(this,"Select action:","Road Management",
                JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
        if(choice==0) {
            String loc=StyledDialog.input(this,"Intersection to block:","Block Road"); if(loc==null)return;
            loc=resolveLocation(loc);
            String reason=StyledDialog.input(this,"Reason:","Block Reason");
            if(reason==null)reason="Road blocked";
            manager.blockIntersection(loc,reason); mapPanel.repaint(); log("BLOCK    "+loc+": "+reason);
        } else if(choice==1) {
            String loc=StyledDialog.input(this,"Intersection to unblock:","Unblock Road"); if(loc==null)return;
            loc=resolveLocation(loc);
            manager.unblockIntersection(loc); mapPanel.repaint(); log("UNBLOCK  "+loc);
        }
    }

    private void clearIncidents() {
        if(StyledDialog.confirm(this,"Clear all resolved incidents?","Confirm")) {
            manager.clearResolvedIncidents(); mapPanel.repaint(); log("CLEANUP  Resolved incidents cleared");
        }
    }

    private void export() {
        String ts=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fn="smart_city_"+ts+".txt";
        try(PrintWriter w=new PrintWriter(new FileWriter(fn))) {
            w.println("SMART CITY EXPORT  —  "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            w.println("─".repeat(70));
            w.println("VEHICLES");
            for(Vehicle v:manager.getVehicles().values())
                w.printf("  %s %-8s  %-12s  (%.1f,%.1f)  %s%n",v.type.getIcon(),v.id,v.type.getDisplayName(),v.lat,v.lon,v.getCurrentStatus());
            w.println("\nINCIDENTS");
            for(Incident i:manager.getAllIncidents())
                w.printf("  [%s] %-8s  %-10s  %s  %s%n",i.resolved?"✓":"!",i.location,i.severity.getDisplayName(),i.description,i.getFormattedTime());
            w.println("\nMARKERS");
            for(MapMarker m:manager.getMarkers().values())
                w.printf("  %s %-15s  %s%n",m.icon,m.label,m.intersectionId);
            w.println("\nBLOCKED ROADS");
            for(String b:manager.getBlockedRoads()) w.println("  "+b);
            log("EXPORT   Saved to "+fn);
            StyledDialog.info(this,"Exported to "+fn,"Export Complete");
        } catch(IOException ex) { StyledDialog.warn(this,"Export error: "+ex.getMessage(),"Error"); }
    }

    private String resolveLocation(String input) {
        String res=manager.searchAddress(input);
        return res!=null ? res : input.toUpperCase();
    }

    private void log(String msg) { logPanel.append(msg); }
}

// ==================== HEADER ====================
class HeaderPanel extends JPanel {
    private final JLabel timeLbl;
    private final JLabel statusLbl;

    HeaderPanel() {
        setPreferredSize(new Dimension(0,64));
        setOpaque(false);

        timeLbl = new JLabel("--:--:--");
        timeLbl.setFont(new Font("Consolas", Font.BOLD, 16));
        timeLbl.setForeground(DS.ACCENT_CYAN);

        statusLbl = new JLabel("● SYSTEM OPERATIONAL");
        statusLbl.setFont(DS.FONT_BADGE);
        statusLbl.setForeground(DS.SAFE);

        new javax.swing.Timer(1000, e -> {
            timeLbl.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            timeLbl.repaint();
        }).start();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(),h=getHeight();

        // Gradient bar
        GradientPaint gp=new GradientPaint(0,0,new Color(0x060B18),w,0,new Color(0x0C1830));
        g2.setPaint(gp);
        g2.fillRect(0,0,w,h);

        // Bottom border with glow
        g2.setColor(DS.BORDER_SUBTLE);
        g2.drawLine(0,h-1,w,h-1);
        GradientPaint glow=new GradientPaint(w/2-200,h-4,new Color(0,200,255,60),w/2,h-4,new Color(0,200,255,0),true);
        g2.setPaint(glow);
        g2.fillRect(0,h-4,w,4);

        // Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(DS.TEXT_PRIMARY);
        g2.drawString("SMART CITY", 20, 28);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(DS.TEXT_SECONDARY);
        g2.drawString("Traffic & Emergency Management System", 20, 48);

        // Divider
        g2.setColor(DS.BORDER_ACTIVE);
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(w/2, 14, w/2, h-14);

        // Right: time + status
        g2.setFont(new Font("Consolas", Font.BOLD, 20));
        g2.setColor(DS.ACCENT_CYAN);
        String t=LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(t, w-fm.stringWidth(t)-20, 34);

        g2.setFont(DS.FONT_BADGE);
        g2.setColor(DS.SAFE);
        g2.drawString("● OPERATIONAL", w-110, 50);
    }
}

// ==================== MAIN WINDOW ====================
public class SmartCity extends JFrame {

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored){}
        DS.applyGlobalDefaults();
        SwingUtilities.invokeLater(() -> new SmartCity().setVisible(true));
    }

    private final CityManager cityManager;
    private CityMapPanel mapPanel;
    private StatsPanel statsPanel;
    private IncidentPanel incidentPanel;
    private VehiclePanel vehiclePanel;
    private TrafficSignalPanel signalPanel;
    private LogPanel logPanel;

    SmartCity() {
        cityManager = new CityManager();
        initUI();
        startRefresh();
    }

    private void initUI() {
        setTitle("Smart City — Traffic & Emergency Management");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1680, 980);
        setMinimumSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(DS.BG_DEEP);

        mapPanel = new CityMapPanel(cityManager);
        logPanel = new LogPanel();
        ControlPanel ctrlPanel = new ControlPanel(cityManager, mapPanel, logPanel, this);
        statsPanel = new StatsPanel(cityManager);
        incidentPanel = new IncidentPanel(cityManager);
        vehiclePanel = new VehiclePanel(cityManager);
        signalPanel = new TrafficSignalPanel(cityManager);

        // Main layout: header | left-map | right-tabs
        setLayout(new BorderLayout());
        add(new HeaderPanel(), BorderLayout.NORTH);
        add(buildMainSplit(ctrlPanel), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JSplitPane buildMainSplit(ControlPanel ctrl) {
        // Left: map + log
        JPanel leftPanel = new JPanel(new BorderLayout(0,0));
        leftPanel.setBackground(DS.BG_DEEP);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(6,8,6,4));

        // Map in a styled container
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBackground(DS.BG_DEEP);
        mapContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DS.BORDER_ACTIVE, 1),
                null));

        // Zoom toolbar
        JPanel zoomBar = new JPanel(new FlowLayout(FlowLayout.LEFT,6,5));
        zoomBar.setBackground(DS.BG_PANEL);
        zoomBar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,DS.BORDER_SUBTLE));
        for(String[] btn : new String[][]{{"Zoom In","+"}, {"Zoom Out","−"}, {"Reset","⊙"}}) {
            CmdButton b = new CmdButton(btn[1]+" "+btn[0], DS.ACCENT_CYAN);
            b.setFont(new Font("Segoe UI", Font.BOLD, 11));
            if(btn[1].equals("+")) b.addActionListener(e->mapPanel.zoom(1.15));
            else if(btn[1].equals("−")) b.addActionListener(e->mapPanel.zoom(0.85));
            else b.addActionListener(e->mapPanel.resetView());
            zoomBar.add(b);
        }
        JLabel zoomHint = new JLabel("  Scroll wheel to zoom  ·  Drag to pan");
        zoomHint.setFont(DS.FONT_BADGE);
        zoomHint.setForeground(DS.TEXT_MUTED);
        zoomBar.add(zoomHint);

        mapContainer.add(mapPanel, BorderLayout.CENTER);
        mapContainer.add(zoomBar, BorderLayout.SOUTH);

        // Log at bottom of left panel
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mapContainer, logPanel);
        leftSplit.setDividerLocation(650);
        leftSplit.setDividerSize(4);
        leftSplit.setBorder(null);
        leftSplit.setBackground(DS.BG_DEEP);
        leftPanel.add(leftSplit);

        // Right: tabbed panels + controls
        JTabbedPane tabs = buildTabs(ctrl);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, tabs);
        mainSplit.setDividerLocation(1080);
        mainSplit.setDividerSize(4);
        mainSplit.setResizeWeight(0.68);
        mainSplit.setBorder(null);
        mainSplit.setBackground(DS.BG_DEEP);
        return mainSplit;
    }

    private JTabbedPane buildTabs(ControlPanel ctrl) {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(DS.FONT_LABEL);
        tabs.setBackground(DS.BG_BASE);
        tabs.setForeground(DS.TEXT_SECONDARY);
        tabs.setBorder(BorderFactory.createEmptyBorder(6,4,6,8));

        // Custom tab UI
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override protected void paintTabBackground(Graphics g, int tp, int ti, int x, int y, int w, int h, boolean sel) {
                Graphics2D g2=(Graphics2D)g;
                g2.setColor(sel ? DS.BG_PANEL : DS.BG_BASE);
                g2.fillRect(x,y,w,h);
                if(sel) {
                    g2.setColor(DS.ACCENT_CYAN);
                    g2.fillRect(x,y+h-2,w,2);
                }
            }
            @Override protected void paintTabBorder(Graphics g, int tp, int ti, int x, int y, int w, int h, boolean sel) {
                g.setColor(sel ? DS.BORDER_ACTIVE : DS.BORDER_SUBTLE);
                g.drawRect(x,y,w,h);
            }
            @Override protected void paintContentBorder(Graphics g, int tp, int sel) {}
        });

        tabs.addTab("  📊 STATUS  ", statsPanel);
        tabs.addTab("  ⚠️ INCIDENTS  ", incidentPanel);
        tabs.addTab("  🚗 FLEET  ", vehiclePanel);
        tabs.addTab("  🚦 SIGNALS  ", signalPanel);
        tabs.addTab("  🎛 CONTROLS  ", ctrl);

        tabs.setForegroundAt(0, DS.ACCENT_CYAN);
        tabs.setForegroundAt(1, DS.WARN);
        tabs.setForegroundAt(2, DS.SAFE);
        tabs.setForegroundAt(3, DS.ACCENT_VIOLET);
        tabs.setForegroundAt(4, DS.ACCENT_TEAL);

        return tabs;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x06090F));
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,DS.BORDER_SUBTLE));
        bar.setPreferredSize(new Dimension(0, 26));

        JLabel left = new JLabel("  v8.0 — Dark Command Center  ·  Yen's K-Shortest Paths  ·  Real-time Dispatch  ·  Interactive Markers");
        left.setFont(DS.FONT_BADGE);
        left.setForeground(DS.TEXT_MUTED);

        JLabel right = new JLabel("© Smart City System  ");
        right.setFont(DS.FONT_BADGE);
        right.setForeground(DS.TEXT_MUTED);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void startRefresh() {
        new javax.swing.Timer(1500, e -> {
            if(statsPanel!=null) statsPanel.refresh();
            if(incidentPanel!=null) incidentPanel.refresh();
            if(vehiclePanel!=null) vehiclePanel.refresh();
        }).start();
    }
}
