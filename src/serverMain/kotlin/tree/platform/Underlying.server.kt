package net.derfruhling.html.tree.platform

import com.fleeksoft.ksoup.nodes.Attribute
import com.fleeksoft.ksoup.nodes.Comment
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.TextNode
import com.fleeksoft.ksoup.parser.Parser

actual typealias UnderlyingBase = Any
actual typealias UnderlyingElement = Element
actual typealias UnderlyingAttribute = Attribute
actual typealias UnderlyingText = TextNode
actual typealias UnderlyingComment = Comment
actual typealias UnderlyingDocument = Document
actual typealias UnderlyingDocumentFragment = DocumentFragmentElement

class DocumentFragmentElement : Element("#fragment", Parser.NamespaceHtml)
