#lol

##
# Whether to preseve links in output
#
keepLinks = True

# handle 'a' separetely, depending on keepLinks
ignoredTags = [
         'big', 'blockquote', 'center', 'cite', 'div', 'em',
        'font', 'h1', 'h2', 'h3', 'h4', 'hiero',  'kbd', 'nowiki',
        's', 'tt', 'var',
]

# This is obtained from the dump itself
prefix = None # Lost?

##
# Whether to transform sections into HTML
#
keepSections = True

##
# Drop these elements from article text
#
discardElements = set([
        'gallery', 'timeline', 'noinclude', 'pre',
        'table', 'tr', 'td', 'th', 'caption',
        'form', 'input', 'select', 'option', 'textarea',
        'ul', 'li', 'ol', 'dl', 'dt', 'dd', 'menu', 'dir',
        'ref', 'references', 'img', 'imagemap', 'source'
        ])

selfClosingTags = [ 'br', 'hr', 'nobr', 'ref', 'references' ]

lang=""

placeholder_tags = { 'code':'codice'}

convert=True
