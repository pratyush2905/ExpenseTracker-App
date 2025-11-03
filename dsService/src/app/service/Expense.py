from typing import Optional
from langchain_mistralai import ChatMistralAI
from langchain_core.pydantic_v1 import BaseModel, Field
import re

class Expense(BaseModel):
    """Information about a transaction made on any Card"""
    user_id: Optional[str] = Field(title="user_id", description="user id of the user")
    amount: Optional[str] = Field(title="expense", description="Expense made on the transaction")
    merchant: Optional[str] = Field(title="merchant", description="Merchant name whom the transaction has been made")
    currency: Optional[str] = Field(title="currency", description="currency of the transaction")

    def serialize(self):
        # Normalize amount: drop thousands separators, keep decimal point
        normalized_amount = None
        if self.amount is not None:
            # remove commas and spaces
            amt = re.sub(r"[\s,]", "", str(self.amount))
            normalized_amount = amt

        normalized_currency = (self.currency or "INR").upper()

        return {
            "user_id": self.user_id,
            "amount": normalized_amount,
            "merchant": self.merchant,
            "currency": normalized_currency
        }