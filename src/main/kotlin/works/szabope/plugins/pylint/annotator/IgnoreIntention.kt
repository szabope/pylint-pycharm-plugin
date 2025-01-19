package works.szabope.plugins.pylint.annotator

/**
 * TODO: intention for inline suppression
 * Example: # pylint: disable=locally-disabled, multiple-statements, fixme, line-too-long
 * Precondition: algorithm for comment placement
 *  e.g. column == 0 doesn't always mean 'previous line', e.g. missing-final-newline
 */
class IgnoreIntention
