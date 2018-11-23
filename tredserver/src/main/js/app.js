'use strict';

const React = require('react');
const ReactDOM = require('react-dom')
const client = require('./client');

import {FormattedRelative} from 'react-intl';
import {IntlProvider} from 'react-intl';
import NumericInput from 'react-numeric-input';

const Datetime = require('react-datetime');
const ReactHighcharts = require('react-highcharts'); // Expects that Highcharts was loaded in the code.


ReactHighcharts.Highcharts.setOptions(
    {
        global: {
            useUTC: false
        }
    });


function getLang() {
    if (navigator.languages != undefined)
        return navigator.languages[0];
    else
        return navigator.language;
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
            this.setState({interps: response.entity._embedded.levelInterpolations});
        });
        client({method: 'GET', path: '/api/pumpInstructions'}).done(response => {
            var instMap = {};
            for (var i = 0, len = response.entity._embedded.pumpInstructions.length; i < len; i++) {
                instMap[response.entity._embedded.pumpInstructions[i].name] = response.entity._embedded.pumpInstructions[i]
            }
            this.setState({instructions: instMap});
        });
    }

    getLevels() {
        client({method: 'GET', path: '/pump/levels'}).done(response => {
            this.setState({
                levelVals: response.entity.levels,
                powerVals: response.entity.powers,
                depth: response.entity.depth,
                depthFiveMin: response.entity.depthFiveMin
            });
        });
    }

    updateInstruction(instruction) {
        this.state.instructions[instruction.name] = instruction;
        this.setState({instructions: this.state.instructions});
    }

    render() {
        var sides = []
        var headers = [];
        var instructions = [];
        if (this.state.interps) {
            var arrayLength = this.state.interps.length;
            for (var i = 0; i < arrayLength; i++) {
                headers.push(<th>Pump {i} {pageType}</th>);
                sides.push(<td><LevelInterpolation interp={this.state.interps[i]} side={i}/></td>)
            }
        }
        if (this.state.instructions) {
            for (var instName in this.state.instructions) {
                instructions.push(<tr>
                    <td colSpan={2}><PumpInstruction instruction={this.state.instructions[instName]}
                                                     updateInstruction={this.updateInstruction}/></td>
                </tr>);
            }
        }
        return (
            <div>Pump Control <span
                style={{"color": "#8866ff", "fontSize": "90%"}}> Waterline = {this.state.depthFiveMin} <span
                style={{"fontStyle": "italic"}}>( now : {this.state.depth})</span></span>
                <table>
                    <tbody>
                    <tr>
                        {headers}
                    </tr>
                    <WaterLevel powerVals={this.state.powerVals} levelVals={this.state.levelVals}/>
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

class PumpInstruction extends React.Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    render() {
        return (
            <form>

			<textarea name={this.props.instruction.name} placeholder="instruction" rows="4" cols="60"
                      defaultValue={this.props.instruction.value}
                      onChange={this.handleChange}/>
                <p>{this.props.instruction.error}</p>

                <button onClick={this.handleSubmit}>Save</button>

            </form>
        )
    }

    handleSubmit(e) {
        e.preventDefault();
        client({
            method: 'PUT',
            path: this.props.instruction._links.self.href,
            entity: this.props.instruction,
            headers: {'Content-Type': 'application/json'}
        }).done(response => {
            this.props.updateInstruction(response.entity);
        });
    };


    handleChange(e) {
        this.props.instruction.value = e.target.value;
    }

}


class WaterLevel extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        var levels = [];
        var indexCount = 0;
        if (this.props.levelVals) {
            for (var x in this.props.levelVals) {
                levels.push(<td>Level = {this.props.levelVals[indexCount]} inch(s) <p/> Power
                    = {this.props.powerVals[indexCount]} </td>);
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


class LevelInterpolation extends React.Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    render() {
        var levels = [];
        var indexCount = 0;
        if (this.props.interp.levels) {
            for (var x in this.props.interp.levels) {
                levels.push(<InterpEntry levels={this.props.interp.levels} side={this.props.side} left={x}
                                         value={this.props.interp.levels[x]}/>)
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
        client({
            method: 'PUT',
            path: this.props.interp._links.self.href,
            entity: this.props.interp,
            headers: {'Content-Type': 'application/json'}
        });
    }
}

class InterpEntry extends React.Component {

    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
    }

    render() {
        return (
            <tr>
                <td>{this.props.left}</td>

                <td><input type="text" placeholder="raw val" size="6"
                           defaultValue={this.props.value}
                           onChange={this.handleChange}/>
                </td>
            </tr>
        )
    }

    handleChange(e) {
        this.props.levels[this.props.left] = e.target.value;
    }
}


class OverviewApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    render() {
        return (
            <IntlProvider locale={getLang()}>
                <div>
                    <AlertOverview/>
                    <CommonActions/>
                    <TemperatureOverview/>
                    <TemperatureChart/>
                    <WaterLevelOverview/>
                    <WaterLevelChart/>
                    <SensorOverview/>
                    <SensorChart/>
                    <EnvOverview/>
                    <EnvChart/>
                    <AdminActions/>
                </div>
            </IntlProvider>
        )
    }
}

class AlertOverview extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
        this.getAlerts = this.getAlerts.bind(this);
    }

    componentDidMount() {
        this.getAlerts();
        setInterval(this.getAlerts, 10000);
    }

    getAlerts() {
        client({method: 'GET', path: '/history/alerts'}).done(response => {
            this.setState({alerts: response.entity, active: (response.entity[0] && response.entity[0].active)});
        });
    }

    render() {
        const hasData = (this.state.alerts && this.state.alerts.length > 0);
        var dateMessage = "";
        var alertMessage = [];
        if (hasData) var issueDate = new Date(this.state.alerts[0].alertTime);
        var issueCount = 0;
        if (this.state.alerts && this.state.active) issueCount = this.state.alerts.length;
        if (hasData) {
            dateMessage = <div style={{"float": "right"}}><FormattedRelative value={issueDate}/></div>
            for (var x in this.state.alerts) {
                alertMessage.push(<div id="alert_message" key={x}> {this.state.alerts[x].message} </div>);
            }
        }
        return (
            <div>
                <div id={this.state.active ? "alert_top_active" : "alert_top_inactive"}>
                    <div style={{"float": "left"}}> {issueCount} Active Alert{issueCount != 1 ? "s" : ""}  </div>
                    {dateMessage}
                </div>
                <div id="alert_bottom"> {alertMessage} </div>
            </div>
        )
    }
}

class CommonActions extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
    }


    render() {
        var viewControlTag = [0, 0, 200, 500].join(' ')
        return (
            <div>
                <div id="control_bar">
                    <svg id="control_tag" viewBox={viewControlTag}>
                        <rect x={0} y={0} rx={80} ry={80} width={400} height={500} fill={"#D5D8C8"}/>
                        <text fontFamily={"Alegreya Sans"} fill={"#17A598"} fontSize={140}
                              transform={" rotate(-90 0,0) translate(-440,150) "}>Pumps
                        </text>
                    </svg>
                    <button id="control_button_g" style={{"width": "22%"}} onClick={this.handleClick}
                            formAction="/pump/allOn">Enabled
                    </button>
                    <div id="control_gap"><p></p></div>
                    <button id="control_button_o" style={{"width": "22%"}} onClick={this.handleClick}
                            formAction="/pump/allOff">Pause (5m)
                    </button>
                    <div id="control_gap"><p></p></div>
                    <button id="control_button_g" style={{"width": "22%"}} onClick={this.handleClick}
                            formAction="/pump/topoffOn">Fill On
                    </button>
                    <div id="control_gap"><p></p></div>
                    <button id="control_button_o" style={{"width": "22%"}} onClick={this.handleClick}
                            formAction="/pump/topoffDisable">Fill Off(24h)
                    </button>
                </div>
            </div>
        )
    }

    handleClick(e) {
        e.preventDefault();
        client({method: 'POST', path: e.target.formAction});
    };
}

class TemperatureChart extends React.Component {


    constructor(props) {
        super(props);
        this.state = {
            config: {
                title: {
                    title: {
                        text: null
                    },
                },
                chart: {
                    height: '230',
                },
                legend: {
                    enabled: false
                },
                yAxis: {
                    title: {text: null}
                },
                time: {
                    useUTC: false
                },
                series: [{
                    name: "Loading...",
                    data: [[0, 0]]
                    },
                    {
                        name: "Loading...",
                        data: [[0, 0]]
                    }, {
                        name: "Loading...",
                        data: [[0, 0]]
                    }]
            }
        };
        this.getTempHistory = this.getTempHistory.bind(this);
    }

    componentDidMount() {
        this.getTempHistory();
        setInterval(this.getTempHistory, 5 * 60 * 1000);
    }


    getTempHistory() {
        client({
            method: 'GET',
            path: '/history/series?filters=Top Temperature,Bottom Temperature,Outside Temperature'
        }).done(response => {
            this.setState({
                config: {
                    plotOptions: {
                        line: {
                            marker: {
                                enabled: false
                            }
                        }
                    },
                    credits: {
                        enabled: false
                    },
                    chart: {
                        height: '230',
                        backgroundColor: '#17A598',
                        plotBackgroundColor: '#FFFFFF'
                    },
                    title: {
                        text: null
                    },
                    legend: {
                        enabled: false
                    },
                    yAxis: {
                        title: {text: null},
                        labels: {
                            style: {
                                color: '#FFFFFF',
                                fontSize: '15px'
                            }
                        }
                    },
                    xAxis: {
                        type: 'datetime',
                        labels: {
                            style: {
                                color: '#FFFFFF',
                                fontSize: '15px'
                            }
                        }
                    },
                    series: response.entity
                }
            });
        });
    }

    render() {
        var config = this.state.config;
        config.series[0].color = '#3F8782';
        config.series[1].color = '#8A888B';
        config.series[2].color = '#018BAF';

        var viewControlTag = [0, 0, 200, 1500].join(' ')
        return (
            <div id="chart_bar">
                <svg id="chart_tag" viewBox={viewControlTag}>
                    <rect x={0} y={-100} rx={80} ry={80} width={400} height={1600} fill={"#17A598"}/>
                    <text fontFamily={"Alegreya Sans"} fill={"#F0F0F1"} fontSize={140}
                          transform={" rotate(-90 0,0) translate(-1060,150) "}>Temperature
                    </text>
                </svg>
                <div id="chart">
                    <ReactHighcharts config={config}></ReactHighcharts>
                </div>
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
            this.setState({
                topTemp: response.entity.topTemp.toFixed(2),
                bottomTemp: response.entity.bottomTemp.toFixed(2),
                outTemp: response.entity.outTemp.toFixed(2),
            });
        });
    }

    render() {

        return (
            <div id='score_bar'>
                <div id='score_item' style={{"width": "33%", "backgroundColor": "#018BAF"}}>
                    Top {this.state.topTemp} °F
                </div>
                <div id='score_item' style={{"width": "33%", "backgroundColor": "#3F8782"}}>
                    Bottom {this.state.bottomTemp} °F
                </div>
                <div id='score_item' style={{"width": "33%", "backgroundColor": "#F0806C"}}>
                    Outside {this.state.outTemp} °F
                </div>
            </div>
        )
    }
}

class WaterLevelChart extends React.Component {


    constructor(props) {
        super(props);
        this.state = {
            config: {
                title: {
                    title: {
                        text: null
                    },
                },
                chart: {
                    height: '230',
                },
                legend: {
                    enabled: false
                },
                yAxis: {
                    title: {text: null}
                },
                time: {
                    useUTC: false
                },
                series: [{
                    name: "Loading...",
                    data: [[0, 0]]
                },
                    {
                        name: "Loading...",
                        data: [[0, 0]]
                    }, {
                        name: "Loading...",
                        data: [[0, 0]]
                    }]

            }
        };
        this.getLevelHistory = this.getLevelHistory.bind(this);
    }

    componentDidMount() {
        this.getLevelHistory();
        setInterval(this.getLevelHistory, 5 * 60 * 1000);
    }


    getLevelHistory() {
        client({
            method: 'GET',
            path: '/history/series?filters=Right Pump Level,Left Pump Level,Topoff Count'
        }).done(response => {
            this.setState({
                config: {
                    plotOptions: {
                        area: {
                            stacking: 'normal',
                            marker: {
                                enabled: false
                            }
                        }
                    },
                    credits: {
                        enabled: false
                    },
                    chart: {
                        type: 'area',
                        height: '230',
                        backgroundColor: '#17A598',
                        plotBackgroundColor: '#FFFFFF'
                    },
                    title: {
                        text: null
                    },
                    legend: {
                        enabled: false
                    },
                    yAxis: {
                        title: {text: null},
                        max: 20,
                        labels: {
                            style: {
                                color: '#FFFFFF',
                                fontSize: '15px'
                            }
                        }
                    },
                    xAxis: {
                        type: 'datetime',
                        labels: {
                            style: {
                                color: '#FFFFFF',
                                fontSize: '15px'
                            }
                        }
                    },
                    series: response.entity
                }
            });
        });
    }

    render() {
        var config = this.state.config;
        config.series[2].color = '#F0806C';
        config.series[1].color = '#3F8782';
        config.series[0].color = '#018BAF';
        config.series[2].index = 0;
        config.series[1].index = 1;
        config.series[0].index = 2;
        var viewControlTag = [0, 0, 200, 1500].join(' ')
        return (
            <div id="chart_bar">
                <svg id="chart_tag" viewBox={viewControlTag}>
                    <rect x={0} y={-100} rx={80} ry={80} width={400} height={1600} fill={"#17A598"}/>
                    <text fontFamily={"Alegreya Sans"} fill={"#F0F0F1"} fontSize={140}
                          transform={" rotate(-90 0,0) translate(-1060,150) "}>Water Level
                    </text>
                </svg>
                <div id="chart">
                    <ReactHighcharts config={config}></ReactHighcharts>
                </div>
            </div>
        )
    }
}

class WaterLevelOverview extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            levelVals: [0, 0, 0],
            powerVals: [0, 0, 0],
            powerModVals: [0, 0],
            headsOn: [false, false],
            headDates: [0, 0]
        };
        this.getLevels = this.getLevels.bind(this);
    }

    componentDidMount() {
        this.getLevels();
        setInterval(this.getLevels, 5000);
    }

    getLevels() {
        client({method: 'GET', path: '/pump/levels'}).done(response => {
            this.setState({
                levelVals: response.entity.levels,
                powerVals: response.entity.powers,
                powerModVals: response.entity.powerMods,
                depth: response.entity.depth,
                depthFiveMin: response.entity.depthFiveMin,
                headsOn: response.entity.heads,
                headDates: response.entity.headDates,
                topOffCount: response.entity.topOffCount
            });
        });
    }


    render() {

        return (
            <div id='score_bar'>
                <div id='score_item' style={{"width": "25%", "backgroundColor": "#018BAF"}}>
                    Left {this.state.levelVals[0].toFixed(2)}″ ({this.state.powerVals[0].toFixed(2)} head {this.state.headsOn[0]})
                </div>
                <div id='score_item' style={{"width": "25%", "backgroundColor": "#3F8782"}}>
                    Right {this.state.levelVals[1].toFixed(2)}″ ({this.state.powerVals[1].toFixed(2)} head {this.state.headsOn[1]})
                </div>
                <div id='score_item' style={{"width": "25%", "backgroundColor": "#8A888B"}}>
                    Total {this.state.depth}″
                </div>
                <div id='score_item' style={{"width": "24%", "backgroundColor": "#F0806C"}}>
                    Topoff Count {this.state.topOffCount}
                </div>
            </div>
        )
    }
}


class SensorChart extends React.Component {


    constructor(props) {
        super(props);
        this.state = {
            config: {
                title: {
                    title: {
                        text: null
                    },
                },
                chart: {
                    height: '230',
                },
                legend: {
                    enabled: false
                },
                yAxis: [{
                    title: {text: null}
                }, {
                    title: {text: null}
                }, {
                    title: {text: null}
                }, {
                    title: {text: null}
                }],
                time: {
                    useUTC: false
                },
                series: [{
                    name: "Loading...",
                    data: [[0, 0]]
                }, {
                    name: "Loading...",
                    data: [[0, 0]]
                }, {
                    name: "Loading...",
                    data: [[0, 0]]
                }, {
                    name: "Loading...",
                    data: [[0, 0]]
                }]

            }
        };
        this.getSensorHistory = this.getSensorHistory.bind(this);
    }

    componentDidMount() {
        this.getSensorHistory();
        setInterval(this.getSensorHistory, 5 * 60 * 1000);
    }


    getSensorHistory() {
        client({method: 'GET', path: '/history/series?filters=PH,Salinity,ORP,DO'}).done(response => {
            this.setState({
                config: {
                    plotOptions: {
                        line: {
                            marker: {
                                enabled: false
                            }
                        }
                    },
                    credits: {
                        enabled: false
                    },
                    chart: {
                        type: 'line',
                        height: '230',
                        backgroundColor: '#17A598',
                        plotBackgroundColor: '#FFFFFF'
                    },
                    title: {
                        text: null
                    },
                    legend: {
                        enabled: false
                    },
                    yAxis: [{
                        title: {text: null},
                        labels: {
                            style: {
                                color: '#111111',
                                fontSize: '14px'
                            }
                        }

                    }, {
                        title: {text: null},
                        labels: {
                            style: {
                                color: '#F0806C',
                                fontSize: '14px'
                            }
                        },
                        opposite: true
                    }, {
                        title: {text: null},
                        labels: {
                            style: {
                                color: '#30608B',
                                fontSize: '14px'
                            }
                        }

                    }, {
                        title: {text: null},
                        labels: {
                            style: {
                                color: '#DB381B',
                                fontSize: '14px'
                            }
                        },
                        opposite: true
                    }],
                    xAxis: {
                        type: 'datetime',
                        labels: {
                            style: {
                                color: '#FFFFFF',
                                fontSize: '15px'
                            }
                        }
                    },
                    series: response.entity
                }
            });
        });
    }

    render() {
        var config = this.state.config;
        // for (i = 0; i < 4 ; i++){
        //     config.series[i].yAxis = i;
        //     config.series[i].color = config.yAxis[i].labels.style.color;
        // }
        config.series[3].color = '#DB381B';
        config.series[2].color = '#30608B';
        config.series[1].color = '#F0806C';
        config.series[0].color = '#111111';
        config.series[3].yAxis = 3;
        config.series[2].yAxis = 2;
        config.series[1].yAxis = 1;
        config.series[0].yAxis = 0;
        var viewControlTag = [0, 0, 200, 1500].join(' ')
        return (
            <div id="chart_bar">
                <svg id="chart_tag" viewBox={viewControlTag}>
                    <rect x={0} y={-100} rx={80} ry={80} width={400} height={1600} fill={"#17A598"}/>
                    <text fontFamily={"Alegreya Sans"} fill={"#F0F0F1"} fontSize={140}
                          transform={" rotate(-90 0,0) translate(-1060,150) "}>Sensors
                    </text>
                </svg>
                <div id="chart">
                    <ReactHighcharts config={config}></ReactHighcharts>
                </div>
            </div>
        )
    }
}

class SensorOverview extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
        this.getSensorData = this.getSensorData.bind(this);
    }

    componentDidMount() {
        this.getSensorData();
        setInterval(this.getSensorData, 10000);
    }

    getSensorData() {
        client({method: 'GET', path: '/environment/readings'}).done(response => {
            this.setState({
                ph: (response.entity.ph).toFixed(2),
                orp: (response.entity.orp).toFixed(0),
                salinity: (response.entity.salinity).toFixed(0),
                do: (response.entity.dissolvedO).toFixed(2),
            });
        });
    }


    render() {

        return (
            <div id='score_bar'>
                <div id='score_item' style={{"width": "25%", "backgroundColor": "#30608B"}}>
                    PH {this.state.ph}
                </div>
                <div id='score_item' style={{"width": "25%", "backgroundColor": "#DB381B"}}>
                    Salinity (sg) {this.state.salinity}
                </div>
                <div id='score_item' style={{"width": "25%", "backgroundColor": "#F0806C"}}>
                    ORP (mv) {this.state.orp}
                </div>
                <div id='score_item' style={{"width": "24%", "backgroundColor": "#111111"}}>
                    DO (mg/L) {this.state.do}
                </div>
            </div>
        )
    }
}

class EnvChart extends React.Component {


    constructor(props) {
        super(props);
        this.state = {
            config: {
                title: {
                    title: {
                        text: null
                    },
                },
                chart: {
                    height: '230',
                },
                legend: {
                    enabled: false
                },
                yAxis: [{
                    title: {text: null}
                }, {
                    title: {text: null}
                }, {
                    title: {text: null}
                }, {
                    title: {text: null}
                }],
                time: {
                    useUTC: false
                },
                series: [{
                    name: "Loading...",
                    data: [[0, 0]]
                }, {
                    name: "Loading...",
                    data: [[0, 0]]
                }, {
                    name: "Loading...",
                    data: [[0, 0]]
                }, {
                    name: "Loading...",
                    data: [[0, 0]]
                }]

            }
        };
        this.getSensorHistory = this.getSensorHistory.bind(this);
    }

    componentDidMount() {
        this.getSensorHistory();
        setInterval(this.getSensorHistory, 5 * 60 * 1000);
    }


    getSensorHistory() {
        client({method: 'GET', path: '/history/series?filters=CO2,TVOC,Pressure,Humidity'}).done(response => {
            this.setState({
                config: {
                    plotOptions: {
                        line: {
                            marker: {
                                enabled: false
                            }
                        }
                    },
                    credits: {
                        enabled: false
                    },
                    chart: {
                        type: 'line',
                        height: '230',
                        backgroundColor: '#17A598',
                        plotBackgroundColor: '#FFFFFF'
                    },
                    title: {
                        text: null
                    },
                    legend: {
                        enabled: false
                    },
                    yAxis: [{
                        title: {text: null},
                        labels: {
                            style: {
                                color: '#30608B',
                                fontSize: '14px'
                            }
                        }

                    }, {
                        title: {text: null},
                        labels: {
                            style: {
                                color: '#111111',
                                fontSize: '14px'
                            }
                        },
                        opposite: true
                    }, {
                        title: {text: null},
                        labels: {
                            style: {
                                color: '#F0806C',
                                fontSize: '14px'
                            }
                        }

                    }, {
                        title: {text: null},
                        labels: {
                            style: {
                                color: '#DB381B',
                                fontSize: '14px'
                            }
                        },
                        opposite: true
                    }],
                    xAxis: {
                        type: 'datetime',
                        labels: {
                            style: {
                                color: '#FFFFFF',
                                fontSize: '15px'
                            }
                        }
                    },
                    series: response.entity
                }
            });
        });
    }

    render() {
        var config = this.state.config;

        config.series[3].color = '#DB381B';
        config.series[2].color = '#F0806C';
        config.series[1].color = '#111111';
        config.series[0].color = '#30608B';
        config.series[3].yAxis = 3;
        config.series[2].yAxis = 2;
        config.series[1].yAxis = 1;
        config.series[0].yAxis = 0;
        var viewControlTag = [0, 0, 200, 1500].join(' ')
        return (
            <div id="chart_bar">
                <svg id="chart_tag" viewBox={viewControlTag}>
                    <rect x={0} y={-100} rx={80} ry={80} width={400} height={1600} fill={"#17A598"}/>
                    <text fontFamily={"Alegreya Sans"} fill={"#F0F0F1"} fontSize={140}
                          transform={" rotate(-90 0,0) translate(-1060,150) "}>Environment
                    </text>
                </svg>
                <div id="chart">
                    <ReactHighcharts config={config}></ReactHighcharts>
                </div>
            </div>
        )
    }
}

class EnvOverview extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
        this.getSensorData = this.getSensorData.bind(this);
    }

    componentDidMount() {
        this.getSensorData();
        setInterval(this.getSensorData, 10000);
    }

    getSensorData() {
        client({method: 'GET', path: '/environment/readings'}).done(response => {
            this.setState({
                pressure: (response.entity.pressure).toFixed(2),
                humidity: (response.entity.humidity).toFixed(2),
                co2: (response.entity.co2).toFixed(0),
                tvoc: (response.entity.tvoc).toFixed(0),
            });
        });
    }


    render() {

        return (
            <div id='score_bar'>
                <div id='score_item' style={{"width": "25%", "backgroundColor":"#30608B" }}>
                    CO2(ppm) {this.state.co2}
                </div>
                <div id='score_item' style={{"width": "25%", "backgroundColor": "#DB381B"}}>
                    TVOC(ppm) {this.state.tvoc}
                </div>
                <div id='score_item' style={{"width": "25%", "backgroundColor":"#F0806C"}}>
                    Pressure (mbar) {this.state.pressure}
                </div>
                <div id='score_item' style={{"width": "24%", "backgroundColor": "#111111" }}>
                    Humidity {this.state.humidity}%
                </div>
            </div>
        )
    }
}


class AdminActions extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
    }


    render() {
        var viewControlTag = [0, 0, 200, 500].join(' ')
        return (
            <div>
                <div id="control_bar">
                    <svg id="control_tag" viewBox={viewControlTag}>
                        <rect x={0} y={0} rx={80} ry={80} width={400} height={500} fill={"#D5D8C8"}/>
                        <text fontFamily={"Alegreya Sans"} fill={"#17A598"} fontSize={140}
                              transform={" rotate(-90 0,0) translate(-440,150) "}>Setup
                        </text>
                    </svg>
                    <button id="control_button_r" style={{"width": "22%"}} onClick={this.handleClick}
                            formAction="/reset">Reset Hardware
                    </button>
                    <div id="control_gap"><p></p></div>
                    <a id="control_button_g" style={{"width": "22%"}} href="/pump">Sensors</a>
                    <div id="control_gap"><p></p></div>
                    <button id="control_button_r" style={{"width": "22%"}} onClick={this.handleClick}
                            formAction="/reboot">Reset Server
                    </button>
                    <div id="control_gap"><p></p></div>
                    <button id="control_button_r" style={{"width": "22%"}} onClick={this.handleClick}
                            formAction="/update">Update Firmware
                    </button>
                </div>
            </div>
        )
    }

    handleClick(e) {
        e.preventDefault();
        client({method: 'POST', path: e.target.formAction});
    };


}

class HistoryApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    render() {
        return (
            <IntlProvider locale={getLang()}>
                <div>
                    <HistoryParmChooser/>
                </div>
            </IntlProvider>
        )
    }
}

class HistoryParmChooser extends React.Component {

    constructor(props) {
        super(props);
        this.state = {};
        this.handleChange1 = this.handleChange1.bind(this);
        this.handleChange2 = this.handleChange2.bind(this);
    }

    componentDidMount() {
        this.setState({
            date1: new Date(new Date().getTime()-86400000),
            date2: new Date(),
            interval:10
        });
    }


    render() {

        return (
            <div id='history_bar'>
                <div style={{"float":"left"}}>
                    <Datetime
                        onChange={this.handleChangeD1} value = {this.state.date1} />
                </div>
            <div  style={{"float":"left" }}>
                -
            </div>
                <div style={{"float":"left"}}>
                    <Datetime
                        onChange={this.handleChangeD2} value = {this.state.date2} />
                </div>
                <div  style={{"float":"left"}}>
                    <NumericInput   onChange={this.handleChangeInt} min={10} max={10000} value={this.state.interval} step ={10} size = {1}/>
                </div>

            </div>
        )
    }

    handleChangeD1(d){
        this.setState({date1 : d});
    }
    handleChangeD2(d){
        this.setState({date2 : d});
    }
    handleChangeInt(d){
        this.setState({interval : d});
    }
}


function ElementChooser(props) {
    const page = props.type;
    if (page == "pump")
        return <PumpApp/>
    else if (page == "overview")
        return <OverviewApp/>
    else if (page == "history")
        return <HistoryApp/>

    return null;
}

ReactDOM.render(
    <ElementChooser type={pageType}/>,
    document.getElementById('react_control')
)

