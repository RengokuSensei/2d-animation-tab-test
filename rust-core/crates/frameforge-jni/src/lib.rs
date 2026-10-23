use frameforge_core::color::{ColorEngine, ColorGradingParams};
use frameforge_core::graph::NodeGraph;
use frameforge_core::vector::VectorStroke;
use jni::objects::{JByteBuffer, JClass, JFloatArray, JString};
use jni::sys::{jboolean, jfloat, jint, jstring};
use jni::JNIEnv;

/// Initializes the native FrameForge engine core
#[no_mangle]
pub extern "system" fn Java_com_example_frameforge_nativecore_NativeEngine_nativeInit(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    1 // true
}

/// Applies 144Hz Catmull-Rom spline smoothing to stylus input points
#[no_mangle]
pub extern "system" fn Java_com_example_frameforge_nativecore_NativeEngine_nativeSmoothStroke(
    env: JNIEnv,
    _class: JClass,
    x_array: JFloatArray,
    y_array: JFloatArray,
    pressure_array: JFloatArray,
    tilt_array: JFloatArray,
    subdivisions: jint,
) -> jstring {
    let len = match env.get_array_length(&x_array) {
        Ok(l) => l as usize,
        Err(_) => return std::ptr::null_mut(),
    };

    let mut xs = vec![0.0f32; len];
    let mut ys = vec![0.0f32; len];
    let mut pressures = vec![0.0f32; len];
    let mut tilts = vec![0.0f32; len];

    let _ = env.get_float_array_region(&x_array, 0, &mut xs);
    let _ = env.get_float_array_region(&y_array, 0, &mut ys);
    let _ = env.get_float_array_region(&pressure_array, 0, &mut pressures);
    let _ = env.get_float_array_region(&tilt_array, 0, &mut tilts);

    let mut stroke = VectorStroke::new("stroke", 0xFF000000, 10.0, 1.0);
    for i in 0..len {
        stroke.add_point(xs[i], ys[i], pressures[i], tilts[i]);
    }

    let smoothed = stroke.generate_smooth_path(subdivisions.max(1) as usize);
    let json = serde_json::to_string(&smoothed).unwrap_or_default();

    match env.new_string(json) {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Direct zero-copy color grading on an Android direct byte buffer
#[no_mangle]
pub extern "system" fn Java_com_example_frameforge_nativecore_NativeEngine_nativeApplyColorGrade(
    env: JNIEnv,
    _class: JClass,
    buffer: JByteBuffer,
    width: jint,
    height: jint,
    kelvin: jfloat,
    exposure: jfloat,
    contrast: jfloat,
    saturation: jfloat,
) -> jboolean {
    let raw_addr = match env.get_direct_buffer_address(&buffer) {
        Ok(addr) => addr,
        Err(_) => return 0,
    };

    let expected_len = (width * height * 4) as usize;
    let slice = unsafe { std::slice::from_raw_parts_mut(raw_addr, expected_len) };

    let mut params = ColorGradingParams::default();
    params.kelvin = kelvin;
    params.exposure = exposure;
    params.contrast = contrast;
    params.cdl.saturation = saturation;

    ColorEngine::process_rgba_buffer(slice, &params);
    1
}

/// Auto-arranges a node graph DAG using topological layout in Rust
#[no_mangle]
pub extern "system" fn Java_com_example_frameforge_nativecore_NativeEngine_nativeAutoArrangeGraph(
    mut env: JNIEnv,
    _class: JClass,
    graph_json: JString,
    col_width: jfloat,
    row_height: jfloat,
    start_x: jfloat,
    start_y: jfloat,
) -> jstring {
    let input: String = match env.get_string(&graph_json) {
        Ok(s) => s.into(),
        Err(_) => return std::ptr::null_mut(),
    };

    let mut graph: NodeGraph = match serde_json::from_str(&input) {
        Ok(g) => g,
        Err(_) => return std::ptr::null_mut(),
    };

    graph.auto_arrange(col_width, row_height, start_x, start_y);
    let output = serde_json::to_string(&graph).unwrap_or_default();

    match env.new_string(output) {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}
