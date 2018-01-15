package me.davehummel.tredserver.fish.waterlevel;

import me.davehummel.tredserver.command.math.functions.literal.LiteralU32Int;
import me.davehummel.tredserver.services.ServiceManager;
import me.davehummel.tredserver.command.ImmediateInstruction;
import me.davehummel.tredserver.command.math.InterpType;
import me.davehummel.tredserver.command.math.InterpolationBody;
import me.davehummel.tredserver.command.math.functions.literal.LiteralFloat;
import me.davehummel.tredserver.command.math.functions.literal.LiteralU16Int;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by dmhum_000 on 2/4/2017.
 */
public class LevelInterpUtility {


    public static ImmediateInstruction createInterpInstruction(int id,Map<String,String> map){
        InterpolationBody interp = new InterpolationBody(id);
        boolean isFirst = true;
        TreeMap<Integer,Float> convertedMap = new TreeMap<>();
        for(Map.Entry<String,String> entry:map.entrySet()) {
            int left=0;
            float right=0;
            try{
                left=Integer.parseInt(entry.getValue());
                right=Float.parseFloat(entry.getKey());
            } catch (Exception e){};
            convertedMap.put(left,right);
        }

        for(Map.Entry<Integer,Float> entry:convertedMap.entrySet()){
            if (isFirst){
                isFirst = false;
            }else{
                interp.interpTypes.add(InterpType.LINEAR);
            }
            interp.xVals.add(new LiteralU16Int(entry.getKey()));
            interp.yVals.add(new LiteralFloat(entry.getValue()));
        }
        return new ImmediateInstruction('C',2,interp);
    }

    public static ImmediateInstruction createGyreInterpInstruction(int id,Map<String,String> map, InterpType type){
        InterpolationBody interp = new InterpolationBody(id);
        boolean isFirst = true;
        TreeMap<Long,Float> convertedMap = new TreeMap<>();
        for(Map.Entry<String,String> entry:map.entrySet()) {
            long left=0;
            float right=0;
            try{
                left=Long.parseLong(entry.getKey());
                right=Float.parseFloat(entry.getValue());
            } catch (Exception e){};
            convertedMap.put(left,right);
        }

        for(Map.Entry<Long,Float> entry:convertedMap.entrySet()){
            if (isFirst){
                isFirst = false;
            }else{
                interp.interpTypes.add(type);
            }
            interp.xVals.add(new LiteralU32Int(entry.getKey()));
            interp.yVals.add(new LiteralFloat(entry.getValue()));
        }
        return new ImmediateInstruction('C',2,interp);
    }
}
