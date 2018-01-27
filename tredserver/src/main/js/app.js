'use strict';

const React = require('react');
const ReactDOM = require('react-dom')
const client = require('./client');

const ReactHighcharts = require('react-highcharts'); // Expects that Highcharts was loaded in the code.

var config = {
    xAxis: {
        categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
    },
    series: [{
        data: [29.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 295.6, 454.4]
    }]
};

class PumpApp extends React.Component {

	constructor(props) {
		super(props);
		this.state = {};
		this.getLevels = this.getLevels.bind(this);
	    this.updateInstruction = this.updateInstruction.bind(this);
	}

	componentDidMount() {
		setInterval(this.getLevels, 2000);
		client({method: 'GET', path: '/api/levelInterpolations'}).done(response => {
			this.setState({interps: response.entity._embedded.levelInterpolations} );
		});
		client({method: 'GET', path: '/api/pumpInstructions'}).done(response => {
		    var instMap = {};
		    for (var i = 0, len = response.entity._embedded.pumpInstructions.length; i < len; i++) {
		        instMap[response.entity._embedded.pumpInstructions[i].name]=response.entity._embedded.pumpInstructions[i]
		    }
        	this.setState({instructions: instMap} );
        });
	}

    getLevels() {
        		client({method: 'GET', path: '/pump/levels'}).done(response => {
        			this.setState({levelVals: response.entity.levels , powerVals: response.entity.powers , depth: response.entity.depth , depthFiveMin: response.entity.depthFiveMin} );
        		});
    }

    updateInstruction(instruction){
        this.state.instructions[instruction.name]=instruction;
        this.setState({instructions: this.state.instructions});
    }

	render() {
			var sides = []
			var headers=[];
			var instructions=[];
    		if (this.state.interps){
                var arrayLength = this.state.interps.length;
                for (var i = 0; i < arrayLength; i++) {
                    headers.push(<th>Pump {i} {pageType}</th>);
                    sides.push(<td><LevelInterpolation interp = {this.state.interps[i]} side = {i} /></td>)
                }
    		}
    		if (this.state.instructions){
                for (var instName in this.state.instructions) {
                  instructions.push( <tr> <td colSpan={2} > <PumpInstruction instruction= {this.state.instructions[instName]}
                  updateInstruction={this.updateInstruction}/></td></tr>);
                }
    		}
		return (
		    <div>Pump Control  <span style={{"color":"#8866ff" , "fontSize":"90%"}}> Waterline = {this.state.depthFiveMin} <span style={{ "fontStyle" : "italic" }}>( now : {this.state.depth})</span></span>
		        <table>
            	    <tbody>
            	        <tr>
            	            {headers}
            	         </tr>
            	        <WaterLevel powerVals = {this.state.powerVals} levelVals = {this.state.levelVals}/>
            		    <tr>
            		         {sides}
                         </tr>
                         {instructions}
            		</tbody>
            	</table>
            </div>
		)
	}
}

class PumpInstruction extends React.Component{

    constructor(props) {
		super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
		this.handleChange = this.handleChange.bind(this);
	}

	render() {
		return (
		    <form>

			<textarea name= {this.props.instruction.name} placeholder="instruction" rows="4" cols="60"
                            	defaultValue = {this.props.instruction.value}
                            	onChange={this.handleChange} />
                        <p>{this.props.instruction.error}</p>

			<button onClick={this.handleSubmit}>Save</button>

		    </form>
		)
	}

	handleSubmit(e) {
        e.preventDefault();
        		client({method: 'PUT',
        		 path: this.props.instruction._links.self.href,
        		 entity: this.props.instruction,
                 headers: {'Content-Type': 'application/json'}
        		 }).done(response => {
                           		    this.props.updateInstruction(response.entity);
                 });
    };


    handleChange(e){
    	    this.props.instruction.value=e.target.value;
    }

}


class WaterLevel extends React.Component{

    constructor(props) {
		super(props);
	}

	render() {
        var levels=[];
		var indexCount = 0;
		if ( this.props.levelVals){
            for (var x in this.props.levelVals){
                levels.push(<td>Level = {this.props.levelVals[indexCount]} inch(s) <p/> Power = {this.props.powerVals[indexCount]} </td>);
                indexCount++;
            }
		}

		return (

                <tr>
						{levels}
				</tr>

		)
	}

}


class LevelInterpolation extends React.Component{

    constructor(props) {
		super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
	}

	render() {
		var levels = [];
		var indexCount = 0;
		if (this.props.interp.levels){
            for (var x in this.props.interp.levels){
                levels.push(<InterpEntry levels = {this.props.interp.levels} side = {this.props.side} left = {x} value={this.props.interp.levels[x]}/>)
                indexCount++;
            }
		}

		return (
		<form>
			<table>
				<tbody>
					<tr>
						<th>Height</th>
						<th>Raw Reading</th>
					</tr>
					{levels}
				</tbody>
			</table>
			<button onClick={this.handleSubmit}>Submit {this.props.side}</button>
		</form>
		)
	}

	handleSubmit(e) {
        e.preventDefault();
        		client({method: 'PUT',
        		 path: this.props.interp._links.self.href,
        		 entity: this.props.interp,
                 headers: {'Content-Type': 'application/json'}
        		 });
    }
}

class InterpEntry extends React.Component{

    constructor(props) {
		super(props);
		this.handleChange = this.handleChange.bind(this);
	}

	render() {
		return (
			<tr>
				<td>{this.props.left}</td>

				<td><input type="text" placeholder="raw val" size="6"
                	defaultValue = {this.props.value}
                	onChange={this.handleChange} />
                </td>
			</tr>
		)
	}

	handleChange(e){
	    this.props.levels[this.props.left]=e.target.value;
	}
}

class TempApp extends React.Component {

	constructor(props) {
		super(props);
		this.state = {};
		this.getTemps = this.getTemps.bind(this);
	}

	componentDidMount() {
		setInterval(this.getTemps, 10000);
	}

    getTemps() {
        		client({method: 'GET', path: '/temperature/readings'}).done(response => {
        			this.setState({topTemp: response.entity.topTemp , bottomTemp: response.entity.bottomTemp , outTemp: response.entity.outTemp , topOneHour: response.entity.topOneHour , bottomOneHour: response.entity.bottomOneHour, outOneHour: response.entity.outOneHour} );
        		});
    }

	render() {

		return (
		    <div>Temperature Monitor
		        <table>
            	    <tbody>
            	        <tr>
            	            <td>Top Temperature   <span style={{"color":"#8866ff" }}> {this.state.topOneHour} <span style={{ "fontStyle" : "italic" }}>( now : {this.state.topTemp})</span></span> </td>
            	            <td>Bottom Temperature   <span style={{"color":"#8866ff" }}> {this.state.bottomOneHour} <span style={{ "fontStyle" : "italic" }}>( now : {this.state.bottomTemp})</span></span> </td>
            	            <td>Outside Temperature   <span style={{"color":"#8866ff" }}> {this.state.outOneHour} <span style={{ "fontStyle" : "italic" }}>( now : {this.state.outTemp})</span></span> </td>
            	         </tr>
            		</tbody>
            	</table>
            </div>
		)
	}
}

class OverviewApp extends React.Component {

	constructor(props) {
		super(props);
		this.state = {};

	}

	render() {

		return (
		    <div>Aquarium Monitor
		        <table>
            	    <tbody>
            	            <tr><td><TemperatureOverview/></td></tr>
            	            <tr><td><PumpOverview/></td></tr>
            	            <tr><td><ControlOverview/></td></tr>
							<tr><td>
								<ReactHighcharts config = {config}></ReactHighcharts>
							</td></tr>
            		</tbody>
            	</table>
            </div>
		)
	}
}

class TemperatureOverview extends React.Component {

	constructor(props) {
		super(props);
	    this.state = {};
		this.getTemps = this.getTemps.bind(this);
	}

	componentDidMount() {
	    this.getTemps();
		setInterval(this.getTemps, 10000);
	}

    getTemps() {
        client({method: 'GET', path: '/temperature/readings'}).done(response => {
        			this.setState({topTemp: response.entity.topTemp , bottomTemp: response.entity.bottomTemp , outTemp: response.entity.outTemp , topOneHour: response.entity.topOneHour , bottomOneHour: response.entity.bottomOneHour, outOneHour: response.entity.outOneHour} );
        });
    }

	render() {

		return (
		 <table>
            <tbody>
                <tr>
                    <td className="l1">Temperature</td>
                    <td>Top  <span style={{"color":"#8866ff" }}> {this.state.topOneHour} <span style={{ "fontStyle" : "italic" }}>( now : {this.state.topTemp})</span></span></td>
                    <td>Bot <span style={{"color":"#8866ff" }}> {this.state.bottomOneHour} <span style={{ "fontStyle" : "italic" }}>( now : {this.state.bottomTemp})</span></span></td>
                    <td>Out <span style={{"color":"#8866ff" }}> {this.state.outOneHour} <span style={{ "fontStyle" : "italic" }}>( now : {this.state.outTemp})</span></span></td>
                </tr>
		    </tbody>
		 </table>
		)
	}
}

class PumpOverview extends React.Component {

	constructor(props) {
		super(props);
	    this.state = {levelVals: [0,0,0] , powerVals: [0,0,0] , powerModVals: [0,0] , headsOn:[false,false] , headDates:[0,0]};
	    this.getLevels = this.getLevels.bind(this);
	}

	componentDidMount() {
	    this.getLevels();
    	setInterval(this.getLevels, 2500);
    }


     getLevels() {
        client({method: 'GET', path: '/pump/levels'}).done(response => {
           this.setState({levelVals: response.entity.levels , powerVals: response.entity.powers , powerModVals: response.entity.powerMods , depth: response.entity.depth , depthFiveMin: response.entity.depthFiveMin , headsOn: response.entity.heads , headDates: response.entity.headDates} );
        });
     }

	render() {
		return (
		 <table>
            <tbody>
                <tr>
                    <td className="l1">Water Level</td>
                    <td>
                    Total <span  style={{"color":"#8866ff"}}> {this.state.depthFiveMin} <span style={{ "fontStyle" : "italic" }}>( now : {this.state.depth})</span></span>
                    Left <span style={{"color":"#8866ff" }}> {this.state.levelVals[0]} </span>
                    Right <span style={{"color":"#8866ff" }}> {this.state.levelVals[1]} </span>
                    </td>
                </tr>
                <tr>
                    <td className="l1">Pump Power</td>
                    <td>
                    Left <span style={{"color":"#8866ff" }}> {this.state.powerVals[0]} <span style={{ "fontStyle" : "italic" }}>( x{this.state.powerModVals[0]} )</span></span>
                    Right <span style={{"color":"#8866ff"}}> {this.state.powerVals[1]} <span style={{ "fontStyle" : "italic" }}>( x{this.state.powerModVals[1]} )</span></span>
                    </td>
                </tr>
                <tr>
                    <td className="l1">Topoff System</td>
                    <td>
                        Level<span style={{"color":"#8866ff" }}> {this.state.levelVals[2]} </span>
                        Power <span style={{"color":"#8866ff"}}> {this.state.powerVals[2]} </span>
                    </td>
                 </tr>
                <tr>
                    <td className="l1">Power Heads</td>
                    <td>
                    Left <span style={{"color":"#8866ff" }}> {this.state.headsOn[0]} </span>
                    Right <span style={{"color":"#8866ff"}}> {this.state.headsOn[1]} </span>
                    </td>
                </tr>
            </tbody>
         </table>
		)
	}
}

class ControlOverview extends React.Component {

	constructor(props) {
		super(props);
	 	}

	render() {
		return (
		 <table>
            <tbody>
                <tr>
                    <td className="l1">Pump Control</td>
                    <td>
                    	    <form  >
                    			<button onClick={this.handleClick} formAction="/pump/allOff">Pumps Off</button>
                    		    <button onClick={this.handleClick} formAction="/pump/allOn">Pumps On</button>
                    		    <button onClick={this.handleClick} formAction="/pump/topoffOff">Topoff Off</button>
                                <button onClick={this.handleClick} formAction="/pump/topoffOn">Topoff On</button>
                            </form>
                    </td>
                </tr>
                <tr>
                   <td className="l1">Light Control</td>
                   <td>
                       <form >
                            <button onClick={this.handleClick} formAction="/light/bottomOff">Bottom Off</button>
                            <button onClick={this.handleClick} formAction="/light/bottomOn">Bottom On</button>
                        </form>
                   </td>
                </tr>
                <tr>
                   <td className="l1">System</td>
                   <td>
                           <form >
							   <button onClick={this.handleClick} formAction="/reset">Reset</button>
                           </form>
                   </td>
                </tr>
            </tbody>
         </table>
		)
	}

	handleClick(e) {
        e.preventDefault();
        client({method: 'POST',path:e.target.formAction});
    };



}

function ElementChooser(props){
    const page = props.type;
    if (page == "pump")
        return <PumpApp/>
    else if (page == "temperature")
        return <TempApp/>
    else if (page == "overview")
        return <OverviewApp/>

    return null;
}

ReactDOM.render(
	<ElementChooser type={pageType}/>,
	document.getElementById('react_control')
)

