🏙️ Smart City Traffic & Emergency Management System 📋 Overview A comprehensive Java-based simulation platform that models urban traffic dynamics and emergency response coordination across a smart city infrastructure. The system integrates real-time vehicle tracking, incident management, and intelligent pathfinding algorithms to dispatch emergency vehicles efficiently, manage traffic signals dynamically, and provide interactive visualization for enhanced situational awareness.

✨ Key Features 🗺️ Interactive City Map 4x4 grid of intersections with real-time visualization

Zoom, pan, and drag capabilities

Live vehicle tracking with status indicators

Blocked road visualization with red markers

Pulsing incident alerts with severity levels

Emergency route highlighting

🚑 Emergency Management Automatic and manual emergency vehicle dispatch

Multiple emergency vehicle types: Ambulance, Fire Truck, Police

Real-time status tracking (Available, En Route, On Scene)

Traffic signal preemption for emergency vehicles

Incident reporting and resolution system

🧭 Advanced Pathfinding Dijkstra's Algorithm: Finds optimal shortest path

Yen's K-Shortest Paths Algorithm: Finds up to 10 alternate routes

Dynamic path recalculation considering:

Road blockages

Traffic congestion levels

Emergency vehicle routing

📍 Location & Marker System Address database with 16 predefined locations

Address search by street name, landmark, or intersection ID

Customizable map markers with:

Color selection (6 colors)

Icon selection (10 icons)

Custom labels

Marker management dialog for viewing and removing markers

📊 Real-time Monitoring System statistics dashboard:

Active/emergency vehicles count

Available emergency vehicles

En route vehicles

Active incidents

Blocked roads

Active markers

Live traffic signal status display

Incident tracking panel with severity indicators

Vehicle details panel with location and status

System activity log

📁 Data Export Export system data to text files including:

All markers with locations

Complete address directory

Vehicle fleet status

Active incidents

Blocked roads

System statistics

🛠️ Technical Architecture Data Structures Used Structure Purpose Complexity HashMap O(1) lookups for vehicles, intersections, addresses O(1) average HashSet Blocked roads, edge/node avoidance in algorithms O(1) average ArrayList Ordered collections (incidents, logs, routes) O(1) access PriorityQueue Dijkstra & Yen's algorithms O(log n) Stack Route history O(1) push/pop DefaultListModel Swing UI dynamic lists O(1) updates Algorithms Implemented Dijkstra's Algorithm: Finds shortest path between intersections

Yen's K-Shortest Paths Algorithm: Finds multiple alternate routes

Traffic Simulation: Random vehicle movement and congestion modeling

Emergency Preemption: Traffic signal override for emergency vehicles

Design Patterns Singleton: CityManager centralizes data management

Observer: Timer-based UI updates

Strategy: Multiple pathfinding algorithms

MVC: Separation of data, logic, and UI

🚀 Getting Started Prerequisites Java JDK 8 or higher

Java Swing (included in standard JDK)

Installation Clone the repository

bash git clone https://github.com/yourusername/smart-city-management.git cd smart-city-management Compile the project

bash javac SmartCityGUI.java Run the application

bash java SmartCityGUI Using the Application

Navigation & Map Controls Zoom: Use mouse wheel or zoom buttons
Pan: Click and drag on the map

Reset: Click "🗺️ Reset" button to center view

Address Search & Marking Click "🔍 Search & Mark Address"
Enter street name, landmark, or intersection ID (e.g., "I11")

Select address from results

Option to select location or add marker with custom label/color/icon

Route Planning Click "🗺️ Plan Route" for shortest path
Click "🔄 Find Alternate Routes" for up to 10 alternatives

Routes displayed with distance and path details

Incident Management Click "⚠️ Report Incident"
Specify location, severity (LOW to DISASTER), and description

View active incidents in Incidents tab

Click "✓ Mark Selected as Resolved" to clear incidents

Emergency Dispatch Auto Dispatch: System finds closest available emergency vehicle
Specific Dispatch: Choose from available emergency vehicles

Complete Mission: Mark dispatched vehicle as available

Road Management Click "🚫 Block/Unblock Road"
Block roads for incidents, construction, or special events

View blocked roads in Statistics panel

Data Export Click "📊 Export Data" to save system state
Exported file includes all system data with timestamp

📁 Project Structure text SmartCityGUI.java # Main application entry point ├── CityManager.java # Core data management and business logic ├── CityMapPanel.java # Interactive map visualization ├── ControlPanel.java # User controls and system logs ├── StatsPanel.java # Real-time statistics dashboard ├── IncidentPanel.java # Incident management interface ├── VehiclePanel.java # Vehicle fleet monitoring ├── TrafficSignalPanel.java # Traffic signal status display ├── AddressSearchDialog.java # Address search and marker creation ├── MarkerManagementDialog.java # Marker management ├── GradientPanel.java # Custom UI component ├── RoundedButton.java # Custom UI component └── Enums & Model Classes # Vehicle, Incident, Address, etc. 🎯 Use Cases Emergency Response Scenario Incident reported at intersection I11 with CRITICAL severity

System automatically blocks the intersection

Closest available ambulance (AMB001) is dispatched

Traffic signals along route are preempted

Emergency route is highlighted on map

Vehicle status updates to "En Route"

Upon arrival, mission is marked complete

Road is unblocked and vehicle becomes available

Route Planning Scenario User plans route from I00 to I33

System finds shortest path using Dijkstra's algorithm

User requests alternate routes

Yen's algorithm finds up to 10 different paths

User selects optimal route considering congestion/blockages

🎨 User Interface Top Bar System title and version

Real-time clock display

Main Map (Left Panel) Interactive city grid with 16 intersections

Vehicle positions with status indicators

Incidents with pulsing alerts

Blocked roads with 🚫 markers

Custom markers with colors and icons

Emergency routes highlighted in red

Zoom and pan controls

Side Panels (Right Panel) Statistics: Live system metrics

Incidents: Active incident list with severity

Vehicles: Fleet status with details

Traffic Signals: Real-time signal status

Controls: All management actions

Bottom Bar System status information

Quick tips and instructions

🔧 Configuration City Grid 4x4 grid of intersections (I00 to I33)

Each intersection has coordinates (lat, lon)

Bidirectional connections between adjacent nodes

Vehicle Fleet 12 vehicles including:

3 Ambulances

3 Police Cars

2 Fire Trucks

1 Bus, 1 Car, 1 Taxi

Address Database 16 predefined locations with:

Street names

Landmarks

Coordinates

Intersection IDs

Severity Levels Level Icon Name Description 1 🟢 Low Minor incident 2 🟡 Medium Moderate concern 3 🟠 High Serious situation 4 🔴 Critical Major emergency 5 💀 Disaster Catastrophic event 🧪 Testing Scenarios Manual Testing Checklist Dispatch Test: Report incident → Auto dispatch → Complete mission

Alternate Routes Test: Plan route → Request alternate routes → Block roads → Request routes again

Marker System Test: Search address → Add marker with color/icon → Manage markers → Remove

Traffic Simulation Test: Click "🚦 Simulate Traffic" → Verify movement and congestion

Data Export Test: Click "📊 Export Data" → Verify file creation

🐛 Known Issues & Limitations Grid Size: Fixed 4x4 grid (extendable in future)

Vehicle Movement: Simplified movement simulation

Congestion Model: Basic calculation based on vehicle count

Signal Preemption: Only supports emergency override, no real-time cycle management

Performance: Large-scale extension may require optimization

🔮 Future Enhancements Dynamic city expansion with variable grid sizes

Real-time GPS tracking simulation

Machine learning for traffic prediction

Mobile app integration

Weather impact on traffic

Multi-user support

REST API for external systems

Database persistence

Automated incident detection using AI

Traffic flow optimization algorithms

🙏 Acknowledgments Java Collections Framework

Java Swing GUI Toolkit

Dijkstra's Algorithm (Edsger W. Dijkstra)

Yen's Algorithm (Jin Y. Yen)

Nimbus Look and Feel

📚 Additional Resources Java Documentation

Dijkstra's Algorithm Visualization

Yen's Algorithm Explanation

Java Swing Tutorial

© 2026 Smart City Management System | Built with ❤️ in Java
