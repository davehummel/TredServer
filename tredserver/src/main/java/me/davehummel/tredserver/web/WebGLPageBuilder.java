package me.davehummel.tredserver.web;

import io.netty.buffer.ByteBuf;

/**
 * Created by dmhum on 7/4/2016.
 */
public class WebGLPageBuilder {

    public void appendCanvas(String id,String style,StringBuilder sb){
        sb.append("<canvas id='");
        sb.append(id);
        sb.append("' style='");
        sb.append(style);
        sb.append("' > Your browser doesn't appear to support the <code>&lt;canvas&gt;</code> element. </canvas>");
    }



}
