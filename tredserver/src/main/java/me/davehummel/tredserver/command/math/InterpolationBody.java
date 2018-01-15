package me.davehummel.tredserver.command.math;

import me.davehummel.tredserver.command.InstructionBody;
import me.davehummel.tredserver.command.InstructionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmhum_000 on 5/7/2016.
 */

public class InterpolationBody implements InstructionBody {
    public final int interpID;
    public final List<MathFunc> xVals = new ArrayList<>();
    public final List<MathFunc> yVals = new ArrayList<>();
    public final List<InterpType> interpTypes = new ArrayList<>();

    public InterpolationBody(int interpID) {
        this.interpID = interpID;
    }

    @Override
    public InstructionType getType() {
        return InstructionType.Command;
    }

    @Override
    public void _toString(StringBuilder builder) {
        if (xVals.size()<2)
            throw new NullPointerException("Bad Interpolation, need at least 2 data points");
        if (yVals.size()!=xVals.size())
            throw new NullPointerException("Bad Interpolation, need at least as many x and y datapoints");
        if (interpTypes.size()!=xVals.size()-1)
            throw new NullPointerException("Bad Interpolation, need one interp type between each x,y pair");
        builder.append("INT ");
        builder.append(interpID);
        builder.append(" ");
        addPair(0,builder);
        for (int i = 0; i < interpTypes.size(); i++){
            builder.append(interpTypes.get(i).charVal);
            addPair(i+1,builder);
        }
    }

    private void addPair(int i,StringBuilder builder) {
        xVals.get(i)._toString(builder);
        builder.append(',');
        yVals.get(i)._toString(builder);
    }

    public void clear() {
        xVals.clear();
        yVals.clear();
        interpTypes.clear();
    }
}
