use serde::{Deserialize, Serialize};
use std::collections::HashMap;

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub enum NodeType {
    LayerSource,
    Transform,
    ColorGrade,
    ChromaKey,
    AiMotion,
    Composite,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NodeSocket {
    pub id: String,
    pub name: String,
    pub is_output: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GraphNode {
    pub id: String,
    pub title: String,
    pub node_type: NodeType,
    pub x: f32,
    pub y: f32,
    pub inputs: Vec<NodeSocket>,
    pub outputs: Vec<NodeSocket>,
    pub parameters: HashMap<String, String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NodeConnection {
    pub from_node_id: String,
    pub from_socket: String,
    pub to_node_id: String,
    pub to_socket: String,
}

#[derive(Debug, Clone, Default, Serialize, Deserialize)]
pub struct NodeGraph {
    pub nodes: Vec<GraphNode>,
    pub connections: Vec<NodeConnection>,
}

impl NodeGraph {
    pub fn new() -> Self {
        Self::default()
    }

    /// Auto-arranges nodes in clean columns based on DAG topological depth
    pub fn auto_arrange(&mut self, col_width: f32, row_height: f32, start_x: f32, start_y: f32) {
        // Group nodes by DAG depth
        let mut depth_map: HashMap<String, usize> = HashMap::new();

        // Layer sources start at depth 0
        for node in &self.nodes {
            if node.node_type == NodeType::LayerSource || node.node_type == NodeType::AiMotion {
                depth_map.insert(node.id.clone(), 0);
            }
        }

        // Iteratively propagate depth
        for _ in 0..self.nodes.len() {
            for conn in &self.connections {
                let from_depth = depth_map.get(&conn.from_node_id).copied().unwrap_or(0);
                let to_depth = depth_map.entry(conn.to_node_id.clone()).or_insert(0);
                if from_depth + 1 > *to_depth {
                    *to_depth = from_depth + 1;
                }
            }
        }

        // Layout nodes by column
        let mut col_counts: HashMap<usize, usize> = HashMap::new();
        for node in &mut self.nodes {
            let depth = depth_map.get(&node.id).copied().unwrap_or(0);
            let row = col_counts.entry(depth).or_insert(0);
            node.x = start_x + (depth as f32) * col_width;
            node.y = start_y + (*row as f32) * row_height;
            *row += 1;
        }
    }
}
