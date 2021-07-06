(function(document) {
    document.xpath = function(expr, base) {
        if (!base) base = document;
        var nsResolver = document.createNSResolver((base.ownerDocument == null) ? base.documentElement : base.ownerDocument.documentElement );
        var result = document.evaluate(expr, base, nsResolver, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);
        var nodes = [];
        for (var i = 0 ; i < result.snapshotLength ; i++) {
            nodes.push(result.snapshotItem(i));
        }
        return (nodes.length > 0 ? nodes : null);
    }
})(document);