/*
$('#builder-basic').queryBuilder({
  
  filters: [{
    id: 'ex-text',
    label: 'ex-text',
    type: 'string',
    operators: ['equal', 'not_equal', 'in', 'not_in']
  }, {
    id: 'ex-numeric',
    label: 'ex-numeric',
    type: 'double',
    operators: ['equal', 'not_equal', 'greater', 'less', 'greater_or_equal', 'less_or_equal']
  }, {
    id: 'ex-timestamp',
    label: 'ex-timestamp',
    type: 'datetime',
    placeholder: 'YYYY-MM-DD HH:mm:ss',
    operators: ['greater_or_equal', 'less_or_equal'],
    validation: {
	  format: 'YYYY-MM-DD HH:mm:ss'
    }
  }]
});
*/

function convertJsonToI2AMQuery(json) {
    return jsonToI2AMQuery(json);
}

function jsonToI2AMQuery(jsonObj) {
  if (!jsonObj.hasOwnProperty('condition')) // leaf rule
    return "( " + jsonObj.id + " " + mapOperator(jsonObj.type, jsonObj.operator) + " " + dateTimeToTimeStamp(jsonObj.type, jsonObj.value) + " )"; 
  
  var rules = jsonObj.rules;
  if (rules.length == 1) // only one rule in a group
    return "( " + rules[0].id + " " + mapOperator(rules[0].type, rules[0].operator) + " " + dateTimeToTimeStamp(rules[0].type, rules[0].value) + " )"; 
	
  var query = jsonToI2AMQuery(rules[0]);
  for (i=1; i<rules.length; i++) {
    query = "( " + query + " " + jsonObj.condition + " " + jsonToI2AMQuery(rules[i]) + " )";
  }
  return query;
}

function mapOperator(type, existing) {
  if (type == 'string') {
    switch (existing) {
      case 'equal': 	return 'TEQ';
	  case 'not_equal':	return 'TNQ';
	  case 'in':		return 'TIN';
	  case 'not_in':	return 'TNI';
    }
  } else if (type == 'double') {
    switch (existing) {
      case 'equal': 			return 'NEQ';
	  case 'not_equal': 		return 'NNE';
	  case 'greater': 			return 'NGT';
	  case 'less': 				return 'NLT';
	  case 'greater_or_equal': 	return 'NGE';
	  case 'less_or_equal': 	return 'NLE';
    }
  } else if (type == 'datetime') {
    switch (existing) {
      case 'greater_or_equal':	return 'FROM';
	  case 'less_or_equal': 	return 'TO';
    }
  } else throw 'UNDEFINED I2AM TYPE!!!';
}

function dateTimeToTimeStamp(type, dateTime) {
  if (type == 'datetime')
    return moment(dateTime).valueOf()/1000;
  return dateTime;
}