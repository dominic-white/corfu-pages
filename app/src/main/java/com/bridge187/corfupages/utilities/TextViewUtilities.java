package com.bridge187.corfupages.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.bridge187.corfupages.R;
import java.util.Locale;


public class TextViewUtilities
{

    public enum ButtonType {PHONE, WEBLINK, EMAIL}

    public static void populateTextView(View root, int id, String text)
    {
        TextView textView = (TextView)root.findViewById(id);
        if (checkText(textView, text))
        {
            textView.setText(Html.fromHtml(text));
        }
    }

    public static void populateTextView(View root, int id, String text, String title)
    {
        TextView textView = (TextView)root.findViewById(id);

        if (checkText(textView, text))
        {
            textView.setText(Html.fromHtml(getHtml(title, text)));
        }
    }

    public static void populateButton(Context context, View root, int id, String text, String title, ButtonType buttonType, boolean  displayText)
    {
        Button button = (Button)root.findViewById(id);
        final int buttonWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, context.getResources().getDisplayMetrics());

        if (checkText(button, text))
        {
            if (displayText)
            {
                button.setText(Html.fromHtml(getHtml(title, text)));
            }
            else
            {
                button.setText(Html.fromHtml(getHtml(title, "")));
            }
        }
        button.setWidth(buttonWidth);

        switch (buttonType)
        {

            case PHONE:
                addPhoneClickListener(context, button, text);
                break;
            case WEBLINK:
                addWebClickListener(context, button, text);
                break;
            case EMAIL:
                addEmailClickListener(context, button, text);
                break;
        }
    }

    private static void addEmailClickListener(final Context context, Button button, final String text)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = text.trim();
                String subject = context.getResources().getString(R.string.email_subject);
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts( "mailto", email, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                Intent chooserIntent = Intent.createChooser(emailIntent, context.getResources().getString(R.string.email_chooser));
                chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                context.getApplicationContext().startActivity(chooserIntent);
            }
        });
    }


    private static void addWebClickListener(final Context context, Button button, final String text)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String url = text.trim();
                String lc = url.toLowerCase(Locale.ENGLISH);
                if (!lc.startsWith ("http://") && !lc.startsWith("https://"))
                {
                    url = "http://" + url;
                }
                try
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    intent.setData(Uri.parse(url));
                    context.getApplicationContext().startActivity(intent);
                }
                catch (Exception e)
                {
                    Toast.makeText(context, R.string.website_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private static void addPhoneClickListener(final Context context, Button button, final String text)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String uri = "tel:" + text.trim() ;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.setData(Uri.parse(uri));
                context.getApplicationContext().startActivity(intent);
            }
        });
    }

    private static String getHtml(String title, String text)
    {
        return "<b>".concat(title).concat("</b>").concat(" ").concat(text);
    }

    private static boolean checkText(View view, String text)
    {
        if (text != null && text.length() > 0)
        {
            view.setVisibility(View.VISIBLE);
            return true;
        }
        else
        {
            view.setVisibility(View.GONE);
            return false;
        }
    }
}
