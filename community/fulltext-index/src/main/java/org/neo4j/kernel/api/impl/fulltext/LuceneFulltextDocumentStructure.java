/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.api.impl.fulltext;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.neo4j.values.storable.TextValue;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.ValueGroup;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.document.Field.Store.YES;

public class LuceneFulltextDocumentStructure
{
    public static final String FIELD_ENTITY_ID = "__neo4j__lucene__fulltext__index__internal__id__";

    private static final ThreadLocal<DocWithId> perThreadDocument = ThreadLocal.withInitial( DocWithId::new );

    private LuceneFulltextDocumentStructure()
    {
    }

    private static DocWithId reuseDocument( long id )
    {
        DocWithId doc = perThreadDocument.get();
        doc.setId( id );
        return doc;
    }

    public static Document documentRepresentingProperties( long id, Collection<String> propertyNames, Value[] values )
    {
        DocWithId document = reuseDocument( id );
        document.setValues( propertyNames, values );
        return document.document;
    }

    // The propertyNames *always* line up with the values
    public static Document documentRepresentingPropertiesWithSort( long id, Collection<String> propertyNames, Value[] values, Collection<String> sortProperties, Map<String,String> sortTypes )
    {
        DocWithId document = reuseDocument( id );
        document.setValuesWithSort( propertyNames, values, sortProperties, sortTypes );
        return document.document;
    }

    private static Field encodeValueField( String propertyKey, Value value )
    {
        TextValue textValue = (TextValue) value;
        String stringValue = textValue.stringValue();
        return new TextField( propertyKey, stringValue, NO );
    }

    private static Field encodeSortableValueField( String sortKey, Value value )
    {
        if (value.valueGroup().equals( ValueGroup.NUMBER ))
        {
            if (value.asObject() instanceof Long)
            {
                return new SortedNumericDocValuesField( sortKey, (Long) value.asObject() );
            }
            else if ( value.asObject() instanceof Float )
            {
                return new SortedNumericDocValuesField( sortKey, NumericUtils.floatToSortableInt(
                        (Float) value.asObject() ) );
            }
        }
        if( value.valueGroup().equals( ValueGroup.TEXT ))
        {
            return new SortedDocValuesField( sortKey, new BytesRef( (String) value.asObject() ) );
        }
        return null;
    }

    static long getNodeId( Document from )
    {
        String entityId = from.get( FIELD_ENTITY_ID );
        return Long.parseLong( entityId );
    }

    static Term newTermForChangeOrRemove( long id )
    {
        return new Term( FIELD_ENTITY_ID, "" + id );
    }

    static Query newCountNodeEntriesQuery( long nodeId, String[] propertyKeys, Value... propertyValues )
    {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add( new TermQuery( newTermForChangeOrRemove( nodeId ) ), BooleanClause.Occur.MUST );
        for ( int i = 0; i < propertyKeys.length; i++ )
        {
            String propertyKey = propertyKeys[i];
            Value value = propertyValues[i];
            if ( value.valueGroup() == ValueGroup.TEXT )
            {
                Query valueQuery = new ConstantScoreQuery(
                        new TermQuery( new Term( propertyKey, value.asObject().toString() ) ) );
                builder.add( valueQuery, BooleanClause.Occur.SHOULD );
            }
            else if ( value.valueGroup() == ValueGroup.NO_VALUE )
            {
                Query valueQuery = new ConstantScoreQuery(
                        new WildcardQuery( new Term( propertyKey, "*" ) ) );
                builder.add( valueQuery, BooleanClause.Occur.MUST_NOT );
            }

        }
        return builder.build();
    }

    /**
     * Validates that the sortField is the correct sortType.
     */
    private static boolean validateSortType( Field sortField, String sortType )
    {
        if ( sortType.equalsIgnoreCase( "LONG" ) )
        {
            return sortField instanceof SortedNumericDocValuesField;
        }
        if (sortType.equalsIgnoreCase( "FLOAT" ))
        {
            return sortField instanceof SortedNumericDocValuesField;
        }
        if (sortType.equalsIgnoreCase( "STRING" ))
        {
            return sortField instanceof SortedDocValuesField;
        }
        return false;
    }

    private static class DocWithId
    {
        private final Document document;

        private final Field idField;
        private final Field idValueField;

        private DocWithId()
        {
            idField = new StringField( FIELD_ENTITY_ID, "", YES );
            idValueField = new NumericDocValuesField( FIELD_ENTITY_ID, 0L );
            document = new Document();
            document.add( idField );
            document.add( idValueField );
        }

        private void setId( long id )
        {
            removeAllValueFields();
            idField.setStringValue( Long.toString( id ) );
            idValueField.setLongValue( id );
        }

        private void setValues( Collection<String> names, Value[] values )
        {
            int i = 0;
            for ( String name : names )
            {
                Value value = values[i++];
                if ( value != null && value.valueGroup() == ValueGroup.TEXT )
                {
                    Field field = encodeValueField( name, value );
                    document.add( field );
                }
            }
        }


        // We are guaranteed that
        //         sortProperties.size() >= propertyNames.size() - 1
        // Are we guaranteed any about the order of the sort properties as well???
        // todo: Make sure this works if multiple FT/multiple sort properties are given. What about when we're sorting on a FT prop?
        private void setValuesWithSort( Collection<String> propertyNames, Value[] values, Collection<String> sortProperties, Map<String,String> sortTypes )
        {
            int i = 0;
            Iterator<String> spi = sortProperties.iterator();

            boolean isSortProperty = false;

            for ( String name : propertyNames )
            {
                isSortProperty = false;
                // This will only work if all sortProperties are always after all non-sort props in propertyNames; Is it?
                if ( sortProperties.contains( name ) && spi.hasNext() )
                {
                    String next = spi.next();

                    if ( name.equals( next ) )
                    {
                        isSortProperty = true;
                        Value value = values[i++];
                        if ( value != null )
                        {
                            Field sortableField = encodeSortableValueField( name, value );
                            if ( sortableField != null && validateSortType( sortableField, sortTypes.get( name ) ) )
                            {
                                document.add( sortableField );
                            }
                        }
                    }
                }

                if ( !isSortProperty )
                {
                    Value value = values[i++];
                    if ( value != null && value.valueGroup() == ValueGroup.TEXT )
                    {
                        Field field = encodeValueField( name, value );
                        document.add( field );
                    }
                }
            }
        }

        private void removeAllValueFields()
        {
            Iterator<IndexableField> it = document.getFields().iterator();
            while ( it.hasNext() )
            {
                IndexableField field = it.next();
                String fieldName = field.name();
                if ( !fieldName.equals( FIELD_ENTITY_ID ) )
                {
                    it.remove();
                }
            }
        }
    }
}
