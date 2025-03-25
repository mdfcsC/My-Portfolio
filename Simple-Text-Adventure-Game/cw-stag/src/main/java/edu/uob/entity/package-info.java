/**
 * All entities will need at least a name and a description, some may need additional attributes as well.<br>
 * To make the assignment a little easier, entity names cannot contain spaces (the DOT parser doesn't like it if they do !).<br>
 * In addition to this, entity names defined within the configuration files will also be unique.<br>
 * You won't have to deal with two things called `door` (although your might see a `trapdoor` and a `frontdoor`).<br>
 * As such, you can safely use entity names as unique identifiers.<br>
 * In addition to this, entity objects should be unique within the game.<br>
 * (For example, there should only ever be one instance of the "axe" entity.)<br>
 * <br>
 * Valid player names can consist of uppercase and lowercase letters, spaces, apostrophes and hyphens.<br>
 * No other characters are valid and if they occur, a suitable error message should be returned to the user.<br>
 */
package edu.uob.entity;
