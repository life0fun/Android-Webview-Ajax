function locationData (msg) {
    var html, loc, locs, reslt, _i, _len;
    html = '<ul>';
    reslt = JSON.parse(msg);
    locs = reslt.response.groups[0].items;
    for (_i = 0, _len = locs.length; _i < _len; _i++) {
      loc = locs[_i];
      html += '<li>' + loc.name + '</li>';
    }
    html += '</ul>';
    return html;
};
