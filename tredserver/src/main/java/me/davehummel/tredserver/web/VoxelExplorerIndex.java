/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package me.davehummel.tredserver.web;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * Generates the demo HTML page which is served at http://localhost:8080/
 */
public final class VoxelExplorerIndex {

    private static final String NEWLINE = "\r\n";

    public static ByteBuf getContent(String webSocketLocation) {
        return Unpooled.copiedBuffer(
                "<html><head><title>Voxel Explorer</title></head>" + NEWLINE +
                        "<body onload=\"start()\">" + NEWLINE +
                        "<canvas id=\"glcanvas\" width=\"640\" height=\"480\">\n" +
                        "    Your browser doesn't appear to support the \n" +
                        "    <code>&lt;canvas&gt;</code> element.\n" +
                        "</canvas> <p>" +
                        "<script type=\"text/javascript\">" + NEWLINE +
                        "var socket;" + NEWLINE +
                        "if (!window.WebSocket) {" + NEWLINE +
                        "  window.WebSocket = window.MozWebSocket;" + NEWLINE +
                        '}' + NEWLINE +
                        "if (window.WebSocket) {" + NEWLINE +
                        "  socket = new WebSocket(\"" + webSocketLocation + "\");" + NEWLINE +
                        "  socket.onmessage = function(event) {" + NEWLINE +
                        "    var ta = document.getElementById('responseText');" + NEWLINE +
                        "    ta.value = ta.value + '\\n' + event.data;" + NEWLINE +
                        "   ta.scrollTop = ta.scrollHeight;" +
                        "  };" + NEWLINE +
                        "  socket.onopen = function(event) {" + NEWLINE +
                        "    var ta = document.getElementById('responseText');" + NEWLINE +
                        "    ta.value = \"Web Socket opened!\";" + NEWLINE +
                        "  };" + NEWLINE +
                        "  socket.onclose = function(event) {" + NEWLINE +
                        "    var ta = document.getElementById('responseText');" + NEWLINE +
                        "    ta.value = ta.value + \"Web Socket closed\"; " + NEWLINE +
                        "  };" + NEWLINE +
                        "} else {" + NEWLINE +
                        "  alert(\"Your browser does not support Web Socket.\");" + NEWLINE +
                        '}' + NEWLINE +
                        NEWLINE +
                        "function send(message) {" + NEWLINE +
                        "  if (!window.WebSocket) { return; }" + NEWLINE +
                        "  if (socket.readyState == WebSocket.OPEN) {" + NEWLINE +
                        "    socket.send(message);" + NEWLINE +
                        "  } else {" + NEWLINE +
                        "    alert(\"The socket is not open.\");" + NEWLINE +
                        "  }" + NEWLINE +
                        '}' + NEWLINE +
                        "var gl; // A global variable for the WebGL context\n" +
                        "\n" +
                        "function start() {\n" +
                        "  var canvas = document.getElementById(\"glcanvas\");\n" +
                        "\n" +
                        "  // Initialize the GL context\n" +
                        "  gl = initWebGL(canvas);\n" +
                        "  \n" +
                        "  // Only continue if WebGL is available and working\n" +
                        "  \n" +
                        "  if (gl) {\n" +
                        "    // Set clear color to black, fully opaque\n" +
                        "    gl.clearColor(0.0, 0.0, 0.0, 1.0);\n" +
                        "    // Enable depth testing\n" +
                        "    gl.enable(gl.DEPTH_TEST);\n" +
                        "    // Near things obscure far things\n" +
                        "    gl.depthFunc(gl.LEQUAL);\n" +
                        "    // Clear the color as well as the depth buffer.\n" +
                        "    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);\n" +
                        "  }\n" +
                        "}"+
                        "function initWebGL(canvas) {\n" +
                        "  gl = null;\n" +
                        "  \n" +
                        "  try {\n" +
                        "    // Try to grab the standard context. If it fails, fallback to experimental.\n" +
                        "    gl = canvas.getContext(\"webgl\") || canvas.getContext(\"experimental-webgl\");\n" +
                        "  }\n" +
                        "  catch(e) {}\n" +
                        "  \n" +
                        "  // If we don't have a GL context, give up now\n" +
                        "  if (!gl) {\n" +
                        "    alert(\"Unable to initialize WebGL. Your browser may not support it.\");\n" +
                        "    gl = null;\n" +
                        "  }\n" +
                        "  \n" +
                        "  return gl;\n" +
                        "}"+
                        "</script>" + NEWLINE +
                        "<form onsubmit=\"return false;\">" + NEWLINE +
                        "<input type=\"text\" name=\"message\" style=\"width:400px\" value=\"C G 10 HEADING\"/>" +
                        "<input type=\"button\" value=\"Send\"" + NEWLINE +
                        "       onclick=\"send(this.form.message.value)\" />" + NEWLINE +
                        "<h3>Output</h3>" + NEWLINE +
                        "<textarea id=\"responseText\" style=\"width:500px;height:300px;\"></textarea>" + NEWLINE +
                        "</form>" + NEWLINE +
                        "</body>" + NEWLINE +
                        "</html>" + NEWLINE, CharsetUtil.US_ASCII);
    }

    private VoxelExplorerIndex() {
        // Unused
    }
}
